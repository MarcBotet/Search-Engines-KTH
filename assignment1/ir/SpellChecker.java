/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.util.*;

public class SpellChecker {
    /** The regular inverted index to be used by the spell checker */
    Index index;

    /** K-gram index to be used by the spell checker */
    KGramIndex kgIndex;

    /** The auxiliary class for containing the value of your ranking function for a token */
    class KGramStat implements Comparable {
        double score;
        String token;
        Double sizePostingList;

        KGramStat(String token) {
            this.token = token;
        }

        KGramStat(String token, double score) {
            this.token = token;
            this.score = score;
        }

        KGramStat(String token, double score, Double sizePostingList) {
            this.token = token;
            this.score = score;
            this.sizePostingList = sizePostingList;
        }

        public Double getSizePostingList() {
            if (sizePostingList == null) {
                sizePostingList = (double) index.getPostings(token).size();
            }
            return sizePostingList;
        }

        public String getToken() {
            return token;
        }

        public int compareTo(Object other) {
            if (this.score < ((KGramStat)other).score) return -1;
            if (this.score > ((KGramStat)other).score) return 1;
            if (this.getSizePostingList().equals(((KGramStat) other).getSizePostingList())) return 0;
            return this.getSizePostingList() > ((KGramStat)other).getSizePostingList() ? -1 : 1;
        }

        public String toString() {
            return token + ";" + score;
        }
    }

    /**
     * The threshold for Jaccard coefficient; a candidate spelling
     * correction should pass the threshold in order to be accepted
     */
    private static final double JACCARD_THRESHOLD = 0.4;


    /**
      * The threshold for edit distance for a candidate spelling
      * correction to be accepted.
      */
    private static final int MAX_EDIT_DISTANCE = 2;


    public SpellChecker(Index index, KGramIndex kgIndex) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Computes the Jaccard coefficient for two sets A and B, where the size of set A is 
     *  <code>szA</code>, the size of set B is <code>szB</code> and the intersection 
     *  of the two sets contains <code>intersection</code> elements.
     */
    private double jaccard(int szA, int szB, int intersection) {
        int denominator = szA + szB - intersection;
        return (double) intersection / denominator;
    }

    /**
     * Computing Levenshtein edit distance using dynamic programming.
     * Allowed operations are:
     *      => insert (cost 1)
     *      => delete (cost 1)
     *      => substitute (cost 2)
     */
    private int editDistance(String s1, String s2) {

        if (Math.abs(s2.length()-s1.length()) > MAX_EDIT_DISTANCE) return MAX_EDIT_DISTANCE+1;
        s1 = "#" + s1;
        s2 = "#" + s2;
        int[][] m = new int[s1.length()][s2.length()];

        for (int i = 1; i < s1.length(); ++i) {
            m[i][0] = i;
        }

        for (int j = 1; j < s2.length(); ++j) {
            m[0][j] = j;
        }

        for (int i = 1; i < s1.length(); ++i) {
            for (int j = 1; j < s2.length(); ++j) {
                int substitute = m[i-1][j-1] + ((s1.charAt(i) == s2.charAt(j)) ? 0 : 2);
                int left = m[i-1][j] + 1;
                int up = m[i][j-1] + 1;
                m[i][j] = Math.min(substitute, Math.min(left, up));

            }
        }

        return m[s1.length()-1][s2.length()-1];
    }

    private ArrayList<KGramStat> calculateLevenshteinDistance(ArrayList<String> candidates, String word) {
        ArrayList<KGramStat> answer = new ArrayList<>();
        for (String possible : candidates) {
            int dist = editDistance(possible, word);
            if (dist <= MAX_EDIT_DISTANCE) answer.add(new KGramStat(possible, dist));
        }
        Collections.sort(answer);
        return answer;
    }

    /**
     *  Checks spelling of all terms in <code>query</code> and returns up to
     *  <code>limit</code> ranked suggestions for spelling correction.
     */
    public String[] check(Query query, int limit, QueryType queryType) {
        List<List<KGramStat>> qCorrections = new ArrayList<>();

        for (Query.QueryTerm q : query.queryterm) {
            PostingsList token = index.getPostings(q.term);
            if (token == null) {
                HashSet<String> grams = kgIndex.getKgram(q.term, false);
                ArrayList<String> candidates = jaccardCandidates(grams);
                ArrayList<KGramStat> res = calculateLevenshteinDistance(candidates, q.term);
                int size = Math.min(limit, res.size());
                qCorrections.add(res.subList(0,size));
            } else {
                qCorrections.add(Collections.singletonList(new KGramStat(q.term)));
            }
        }

        return mergeCorrections(qCorrections, limit, queryType).stream().map(KGramStat::getToken).toArray(String[]::new);
    }

    private ArrayList<String> jaccardCandidates(HashSet<String> grams) {
        ArrayList<String> candidates = new ArrayList<>();

        HashMap<KGramPostingsEntry, Integer> options = intersection(grams);
        int size = grams.size();

        for (Map.Entry<KGramPostingsEntry, Integer> entry : options.entrySet()) {
            KGramPostingsEntry post = entry.getKey();
            double jacc = jaccard(size, post.num_grams, entry.getValue());
            if (jacc >= JACCARD_THRESHOLD) {
                candidates.add(kgIndex.getTermByID(post.tokenID));
            }
        }
        return candidates;
    }

    private HashMap<KGramPostingsEntry, Integer> intersection(HashSet<String> grams) {
        HashMap<KGramPostingsEntry, Integer> answer = new HashMap<>();
        for (String gram : grams) {
            for (KGramPostingsEntry entry : kgIndex.getPostings(gram)) {
                answer.merge(entry, 1, Integer::sum);
            }
        }
        return answer;
    }

    /**
     *  Merging ranked candidate spelling corrections for all query terms available in
     *  <code>qCorrections</code> into one final merging of query phrases. Returns up
     *  to <code>limit</code> corrected phrases.
     */
    private List<KGramStat> mergeCorrections(List<List<KGramStat>> qCorrections, int limit, QueryType queryType) {
        if (qCorrections.size() == 1) return qCorrections.get(0);
        List<KGramStat> answer = null;
        for (List<KGramStat> options : qCorrections) {
            if (answer == null) {
                answer = options;
            } else {
                answer = mergeCorrections(answer, options, limit, queryType);
            }
        }

        return answer;
    }

    private List<KGramStat> mergeCorrections(List<KGramStat> w1, List<KGramStat> w2, int limit, QueryType queryType) {
        List<KGramStat> answer = new ArrayList<>();
        Searcher searcher = new Searcher(index, kgIndex);

        for (KGramStat g : w1) {
            String token = g.token;
            double score = g.score;
            double post = g.getSizePostingList();

            String[] list = g.token.split(" ");
            String searchToken = list[list.length-1];

            for (KGramStat g2 : w2) {
                String aux = token + " " + g2.token;
                double sc = score + g2.score;
                Double pa = post + g2.getSizePostingList();
                Query query = new Query(searchToken + " " + g2.token);
                if (searcher.satisfies(query, queryType)) answer.add(new KGramStat(aux, sc, pa));
            }
        }

        Collections.sort(answer);
        return answer.subList(0, Math.min(limit, answer.size()));
    }
}
