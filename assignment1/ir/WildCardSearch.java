package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class WildCardSearch {

    /**
     * The index to be searched by this Searcher.
     */
    Index index;

    /**
     * The k-gram index to be searched by this Searcher
     */
    KGramIndex kgIndex;

    Query query;

    QueryType queryType;

    ArrayList<PostingsList> queryPostings;

    /**
     * Constructor
     *
     * @param index
     * @param kgIndex
     */
    public WildCardSearch(Index index, KGramIndex kgIndex, Query query, QueryType queryType) {
        this.index = index;
        this.kgIndex = kgIndex;
        this.query = query;
        this.queryType = queryType;
    }

    public PostingsList wilcardWord(String term) {
        int position = term.indexOf("*");
        String[] parts = term.split("\\*");
        HashSet<String> Kgrams;
        String regexWord;
        if (parts.length == 1) {
            String word;
            if (position == 0) {
                word = parts[0] + "$";
                regexWord = "\\w*" + word;
            }
            else {
                word = "^" + parts[0];
                regexWord = word + "\\w*";
            }
            Kgrams = kgIndex.getKgram(word, true);
        } else {
            Kgrams = kgIndex.getKgram("^" + parts[0], true);
            HashSet<String> word2 = kgIndex.getKgram(parts[1] + "$", true);
            Kgrams.addAll(word2);
            regexWord = "^" + parts[0] + "\\w*" + parts[1] + "$";
        }

        HashSet<String> queryWords = candidateTokens(Kgrams, regexWord);

        // merge postings over the query words
        PostingsList ans = union(queryWords);
        //ans.weight = queryWords.size();
        return ans;
    }

    public ArrayList<PostingsList> getQeryPostings () {
        return queryPostings;
    }

    private PostingsList union(HashSet<String> queryWords) {
        ArrayList<PostingsList> postingsLists = new ArrayList<>();
        for (String word : queryWords) {
            PostingsList postingsList = index.getPostings(word);
            if (postingsList != null) postingsLists.add(postingsList);
        }

        queryPostings = postingsLists;
        if (queryType.equals(QueryType.RANKED_QUERY)) return null;
        return union(postingsLists);
    }

    private PostingsList union(ArrayList<PostingsList> postingsLists) {
        if (postingsLists.size() == 1) return postingsLists.get(0);
        else if (postingsLists.isEmpty()) return null;
        PostingsList answer = mergePostingList(postingsLists.get(0), postingsLists.get(1));
        for (int i = 2; i < postingsLists.size(); ++i) {
            answer = mergePostingList(answer, postingsLists.get(i));
        }
        return answer;
    }

    private HashSet<String> candidateTokens(HashSet<String> Kgrams, String regexWord) {
        HashSet<String> queryWords = new HashSet<>();
        List<KGramPostingsEntry> postings = null;
        for (String gram : Kgrams) {
            if (postings == null) {
                postings = kgIndex.getPostings(gram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(gram));
            }
        }

        for (KGramPostingsEntry entry : postings) {
            String word = kgIndex.getTermByID(entry.tokenID);
            if (word.matches(regexWord)) {
                queryWords.add(word);
            }
        }
        return queryWords;
    }


    private PostingsList mergePostingList(PostingsList p1, PostingsList p2) {
        PostingsList answer = new PostingsList();
        int i = 0;
        int j = 0;
        while (i < p1.size() && j < p2.size()) {
            PostingsEntry postingsEntry1 = p1.get(i);
            PostingsEntry postingsEntry2 = p2.get(j);
            if (postingsEntry1.docID == postingsEntry2.docID) {
                ArrayList<Integer> offsets = mergeOffsets(postingsEntry1.offsets, postingsEntry2.offsets);
                answer.addEntry(new PostingsEntry(postingsEntry1.docID, offsets));
                ++i;
                ++j;
            } else if (postingsEntry1.docID < postingsEntry2.docID) {
                answer.addEntry(postingsEntry1);
                i++;
            } else {
                answer.addEntry(postingsEntry2);
                ++j;
            }
        }
        if (i < p1.size()) {
            answer.addAll(p1, i);
        } else if (j < p2.size()) {
            answer.addAll(p2, j);
        }
        return answer;
    }

    private ArrayList<Integer> mergeOffsets (ArrayList<Integer> o1, ArrayList<Integer> o2) {
        ArrayList<Integer> answer = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < o1.size() && j < o2.size()) {
            if (o1.get(i).equals(o2.get(j))) {
                answer.add(o1.get(i));
                ++i;
                ++j;
            } else if (o1.get(i) < o2.get(j)) {
                answer.add(o1.get(i));
                i++;
            } else {
                answer.add(o2.get(j));
                ++j;
            }
        }
        if (i < o1.size()) {
            answer.addAll(o1.subList(i, o1.size()));
        } else if (j < o2.size()) {
            answer.addAll(o2.subList(j, o2.size()));
        }
        return answer;
    }
}
