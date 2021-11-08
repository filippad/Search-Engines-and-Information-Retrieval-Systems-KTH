/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.*;

public class PostingsList {
    
    /** The postings postingsMap */
    private ArrayList<Integer> sortedList;
    boolean isSorted = false;
    HashMap<Integer,PostingsEntry> postingsMap = new HashMap<>();

    PostingsList () {
        // Do nothing
    }

    public PostingsList(ArrayList<Integer> sortedList, HashMap<Integer, PostingsEntry> listRe) {
        this.sortedList = sortedList;
        this.postingsMap = listRe;
        this.isSorted = true;
    }

    public PostingsList(HashMap<Integer, PostingsEntry> listRe) {
        this.postingsMap = listRe;
    }

    /** Number of postings in this postingsMap. */
    public int size() {
    return postingsMap.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
        if (!isSorted)
            sortList();
        int docID = sortedList.get(i);
        return postingsMap.get(docID);
    }

    ArrayList<Integer> getSortedList() {
        if (!isSorted)
            sortList();
        return sortedList;
    }

    void sortList () {
        if (!isSorted) {
            sortedList = new ArrayList(postingsMap.keySet());
            Collections.sort(sortedList);
            isSorted = true;
        }
    }

    /**
     * Add an entry to this PostingsList
     * @param docID the document to be added
     */
    void add (int docID, int offset) {
        PostingsEntry entry = postingsMap.get(docID);

        if (entry == null) {
            PostingsEntry newEntry = new PostingsEntry(docID, 0, offset);
            postingsMap.put(docID, newEntry);
        } else {
            entry.addOffset(offset);
        }
    }

    String encode () {
        StringBuilder builder = new StringBuilder();

        if (!isSorted)
            sortList();

        sortedList.forEach(docID -> {
            PostingsEntry entry = postingsMap.get(docID);
            builder.append(entry.encode());
            builder.append("-");
        });

        builder.setLength(builder.length() - 1);
        builder.append("\n");

        return builder.toString();
    }

}

