/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class KGramIndex {

    /** Mapping from term ids to actual term strings */
    HashMap<Integer,String> id2term = new HashMap<Integer,String>();

    /** Mapping from term strings to term ids */
    HashMap<String,Integer> term2id = new HashMap<String,Integer>();

    /** Index from k-grams to list of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<String,List<KGramPostingsEntry>>();

    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K = 3;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    public void save() {
        // HashMap<String,List<KGramPostingsEntry>> index
        try {
            FileOutputStream myFileOutStream
                    = new FileOutputStream(
                    "./kgram/index.txt");

            ObjectOutputStream myObjectOutStream
                    = new ObjectOutputStream(myFileOutStream);

            myObjectOutStream.writeObject(index);

            // closing FileOutputStream and
            // ObjectOutputStream
            myObjectOutStream.close();
            myFileOutStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // HashMap<String,Integer> term2id
        try {
            FileOutputStream myFileOutStream
                    = new FileOutputStream(
                    "./kgram/term2id.txt");

            ObjectOutputStream myObjectOutStream
                    = new ObjectOutputStream(myFileOutStream);

            myObjectOutStream.writeObject(term2id);

            // closing FileOutputStream and
            // ObjectOutputStream
            myObjectOutStream.close();
            myFileOutStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // HashMap<Integer,String> id2term
        try {
            FileOutputStream myFileOutStream
                    = new FileOutputStream(
                    "./kgram/id2term.txt");

            ObjectOutputStream myObjectOutStream
                    = new ObjectOutputStream(myFileOutStream);

            myObjectOutStream.writeObject(id2term);

            // closing FileOutputStream and
            // ObjectOutputStream
            myObjectOutStream.close();
            myFileOutStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        // HashMap<String,List<KGramPostingsEntry>> index
        try {
            FileInputStream fileInput = new FileInputStream(
                    "./kgram/index.txt");

            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            index = (HashMap<String,List<KGramPostingsEntry>>) objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("Class not found");
            obj2.printStackTrace();
            return;
        }
        // HashMap<String,Integer> term2id
        try {
            FileInputStream fileInput = new FileInputStream(
                    "./kgram/term2id.txt");

            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            term2id = (HashMap<String, Integer>) objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("Class not found");
            obj2.printStackTrace();
            return;
        }

        // HashMap<Integer, String> id2term
        try {
            FileInputStream fileInput = new FileInputStream(
                    "./kgram/id2term.txt");

            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            id2term = (HashMap<Integer, String>) objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("Class not found");
            obj2.printStackTrace();
            return;
        }
    }

    /** Generate the ID for an unknown term */
    private int generateTermID() {
        return ++lastTermID;
    }

    public int getK() {
        return K;
    }


    /**
     *  Get intersection of two postings lists
     */
    public List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
        List<KGramPostingsEntry> answer = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < p1.size() && j < p2.size()) {
            KGramPostingsEntry kGramPostingsEntry1 = p1.get(i);
            KGramPostingsEntry kGramPostingsEntry2 = p2.get(j);
            if (kGramPostingsEntry1.tokenID == kGramPostingsEntry2.tokenID) {
                answer.add(kGramPostingsEntry1);
                ++i;
                ++j;
            } else if (kGramPostingsEntry1.tokenID < kGramPostingsEntry2.tokenID) ++i;
            else ++j;
        }

        return answer;
    }

    /** Inserts all k-grams from a token into the index. */
    public void insert( String token ) {
        if (term2id.containsKey(token)) return;
        int id = generateTermID();
        id2term.put(id, token);
        term2id.put(token, id);
        HashSet<String> Kgrams = getKgram(token, false);
        KGramPostingsEntry kGramPostingsEntry = new KGramPostingsEntry(id, Kgrams.size());
        for (String gram : Kgrams) {
            List<KGramPostingsEntry> list = index.get(gram);
            if (list != null) {
                list.add(kGramPostingsEntry);
            } else {
                list = new ArrayList<>();
                list.add(kGramPostingsEntry);
                index.put(gram, list);
            }
        }
    }

    public HashSet<String> getKgram(String token, boolean tokenProcessed) {
        HashSet<String> answer = new HashSet<>();
        if (!tokenProcessed) token = "^" + token + "$";
        for (int i = 0; i <= token.length()-K; ++i) {
            StringBuilder kgram = new StringBuilder();
            for (int j = 0; j < K; ++j) {
                kgram.append(token.charAt(i+j));
            }
            answer.add(kgram.toString());
        }

        return answer;
    }

    /** Get postings for the given k-gram */
    public List<KGramPostingsEntry> getPostings(String kgram) {
        return index.getOrDefault(kgram, new ArrayList<>());
    }

    /** Get id of a term */
    public Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    public String getTermByID(Integer id) {
        return id2term.get(id);
    }

    private static HashMap<String,String> decodeArgs( String[] args ) {
        HashMap<String,String> decodedArgs = new HashMap<String,String>();
        int i=0, j=0;
        while ( i < args.length ) {
            if ( "-p".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("patterns_file", args[i++]);
                }
            } else if ( "-f".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("file", args[i++]);
                }
            } else if ( "-k".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("k", args[i++]);
                }
            } else if ( "-kg".equals( args[i] )) {
                i++;
                if ( i < args.length ) {
                    decodedArgs.put("kgram", args[i++]);
                }
            } else {
                System.err.println( "Unknown option: " + args[i] );
                break;
            }
        }
        return decodedArgs;
    }

    public void specificKgram(String sentence) {
        String[] kgrams = sentence.split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != K) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + K + "-gram");
                System.exit(1);
            }
            if (postings == null) {
                postings = getPostings(kgram);
            } else {
                postings = intersect(postings, getPostings(kgram));
            }
        }
        int resNum = postings.size();
        System.err.println("Found " + resNum + " posting(s) for " + sentence);
    }


    public static void main(String[] arguments) throws FileNotFoundException, IOException {
        HashMap<String,String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
        Tokenizer tok = new Tokenizer( reader, true, false, true, args.get("patterns_file") );
        while ( tok.hasMoreTokens() ) {
            String token = tok.nextToken();
            kgIndex.insert(token);
        }

        String[] kgrams = args.get("kgram").split(" ");
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != k) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + k + "-gram");
                System.exit(1);
            }

            if (postings == null) {
                postings = kgIndex.getPostings(kgram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(kgIndex.getTermByID(postings.get(i).tokenID));
            }
        }
    }
}
