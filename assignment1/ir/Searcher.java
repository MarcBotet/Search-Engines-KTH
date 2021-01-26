/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.Collections;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType ) {

        // list of postingsList fot each token in the query
        ArrayList<PostingsList> postingsLists = new ArrayList<>();
        query.queryterm.forEach((q) -> postingsLists.add(index.getPostings(q.term)));
        //postingsLists.removeAll(Collections.singleton(null));


        if (postingsLists.size() == 1 ) return postingsLists.get(0);
        else if (postingsLists.isEmpty()) return null;

        switch (queryType) {
            case INTERSECTION_QUERY:
                if (postingsLists.contains(null)) return null;
                return searchIntersection(postingsLists);
            case PHRASE_QUERY:
                // do something
            case RANKED_QUERY:
                // do something
            default:
                return postingsLists.get(0); // just to do something
        }
    }

    private PostingsList searchIntersection(PostingsList q1, PostingsList q2) {
        PostingsList answer = new PostingsList();
        int i = 0;
        int j = 0;
        while (i < q1.size() && j < q2.size()) {
            PostingsEntry postingsEntry1 = q1.get(i);
            PostingsEntry postingsEntry2 = q2.get(j);
            if (postingsEntry1.docID == postingsEntry2.docID) {
                answer.addEntry(postingsEntry1);
                ++i;
                ++j;
            } else if (postingsEntry1.docID < postingsEntry2.docID) ++i;
            else ++j;
        }
        return answer;
    }

    private PostingsList searchIntersection(ArrayList<PostingsList> postingsLists) {
        Collections.sort(postingsLists, Collections.reverseOrder());
        PostingsList answer = searchIntersection(postingsLists.get(0), postingsLists.get(1));
        for (int i = 2; i < postingsLists.size(); ++i) {
            answer = searchIntersection(answer, postingsLists.get(i));
        }
        return answer;
    }
}