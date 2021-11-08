/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
    private ArrayList<Integer> offsets = new ArrayList<>();

    PostingsEntry (int docID, double score, int offset) {
        this.docID = docID;
        this.score = score;
        this.offsets.add(offset);
    }

    PostingsEntry (int docID, double score, ArrayList<Integer> offsets) {
        this.docID = docID;
        this.score = score;
        this.offsets = offsets;
    }

    PostingsEntry (int docID, double score) {
        this.docID = docID;
        this.score = score;
    }

    void setScore (double score) {
        this.score = score;
    }

    void addOffset (int offset) {
        offsets.add(offset);
    }

    int getTermFrequency () {
        return offsets.size();
    }

    ArrayList<Integer> getOffsets () {
        return this.offsets;
    }

    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, score );
    }

    String encode () {
        StringBuilder builder = new StringBuilder();

        builder.append(docID);
        builder.append(";");
        offsets.forEach(offset -> {
            builder.append(offset);
            builder.append(",");
        });

        if (offsets.size() > 0)
            builder.setLength(builder.length() - 1);

        return builder.toString();
    }

}

