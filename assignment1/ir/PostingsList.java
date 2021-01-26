/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;

import java.util.ArrayList;

public class PostingsList implements Comparable<PostingsList> {

    /**
     * The postings list
     */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /**
     * Number of postings in this list.
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns the ith posting.
     */
    public PostingsEntry get(int i) {
        return list.get(i);
    }

    public void addEntry(PostingsEntry postingsEntry) {
        if (list.isEmpty()) {
            list.add(postingsEntry);
        } else if (postingsEntry.docID != list.get(list.size() - 1).docID)
            list.add(postingsEntry);
    }

    @Override
    public int compareTo(PostingsList o) {
        return Integer.compare(o.size(), this.size());
    }
}

