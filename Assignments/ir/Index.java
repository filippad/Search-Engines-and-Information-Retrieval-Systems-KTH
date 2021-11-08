/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *  Defines some common data structures and methods that all types of
 *  index should implement.
 */
public interface Index {

    /** Mapping from document identifiers to document names. */
    HashMap<Integer,String> docNames = new HashMap ();
    
    /** Mapping from document identifier to document length. */
    HashMap<Integer,Integer> docLengths = new HashMap ();

    /** Mapping from docName to docID */
    HashMap<String, Integer> docIDs = new HashMap ();

    HashMap<Integer, Double> pageRanks = new HashMap<>();

    HashMap<Integer, String> titleToName = new HashMap<>();

    HashMap<Integer, ArrayList<String>> termsInDocs = new HashMap<>();

    /** Inserts a token into the index. */
    void insert( String token, int docID, int offset );

    /** Returns the postings for a given term. */
    PostingsList getPostings( String token );

    /** This method is called on exit. */
    void cleanup();

}

