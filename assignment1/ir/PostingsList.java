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

    public void addEntry(int docID, int offset) {
        if (list.isEmpty()) {
            list.add(new PostingsEntry(docID, offset));
        } else if (docID != list.get(list.size() - 1).docID)
            list.add(new PostingsEntry(docID, offset));
        else { // is not a new document
            list.get(list.size() - 1).addOffset(offset);
        }
    }

    public void addEntry(PostingsEntry postingsEntry) {
        list.add(postingsEntry);
    }

    public ArrayList<PostingsEntry> getList() {
        return list;
    }

    @Override
    public int compareTo(PostingsList o) {
        return Integer.compare(o.size(), this.size());
    }
}

