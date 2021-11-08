/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<>();

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {

        PostingsList pl = index.get(token);

        // If the token is a new entry
        if (pl == null) {
            // Create an empty PostingsList
            pl  = new PostingsList();
            index.put(token, pl);
        }

        pl.add(docID, offset);

        ArrayList<String> terms = termsInDocs.get(docID);
        if (terms == null) {
            ArrayList<String> newEntry = new ArrayList<>();
            newEntry.add(token);
            termsInDocs.put(docID, newEntry);
        } else if (!terms.contains(token)){
            terms.add(token);
        }

    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        return index.get(token);
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
