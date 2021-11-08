/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.*;


/**
 *  A class for representing a query as a postingsMap of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a postingsMap of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }

    private int termInQuery (String term) {
        for (int i = 0; i < queryterm.size(); i++) {
            QueryTerm qTerm = queryterm.get(i);
            if (term.equals(qTerm.term))
                return i;
        }

        return -1;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback (PostingsList results, boolean[] docIsRelevant, Engine engine) {

        Index index = engine.index;
        if (index instanceof HashedIndex){

            ArrayList<Integer> resultList = results.getSortedList();
            HashMap<Integer, ArrayList<String>> termsInDocs = ((HashedIndex) index).termsInDocs;

            TFIDF tfidf = new TFIDF(index);

            // Step 1: calculate alpha-part for all terms in the old query.
            // alpha-part = 0 for terms that are not in the old query
            for (QueryTerm qTerm : queryterm) {
                qTerm.weight = alpha * qTerm.weight;
            }

            // Step 2: Calculate the number of relevant documents
            int numRelevant = 0;
            for (boolean isRelevant : docIsRelevant) {
                if (isRelevant)
                    numRelevant++;
            }
            int finalNumRelevant = numRelevant;

            // Step 3: Check all words in the relevant documents and calculate their new weights
            for (int i = 0; i < docIsRelevant.length; i++) {
                if (docIsRelevant[i]) {
                    int docID = resultList.get(i);
                    final ArrayList<String> allTerms = termsInDocs.get(docID);

                    allTerms.forEach(term -> {
                        HashMap<Integer,PostingsEntry> map = index.getPostings(term).postingsMap;
                        if (map.get(docID) != null) {
                            double TFDoc = tfidf.getTF(term, docID);

                            double newWeight = beta * TFDoc / finalNumRelevant;

                            // If this term is not yet in the query, add this term to
                            // the new query and initialize its weight.
                            // Otherwise update the weight of the term
                            int j = termInQuery(term);
                            if (j == -1) {
                                QueryTerm newQT = new QueryTerm(term, newWeight);
                                queryterm.add(newQT);
                            } else if (j > -1){
                                double oldWeight = queryterm.get(j).weight;
                                queryterm.get(j).weight = oldWeight + newWeight;
                            }
                        }
                    });
                }
            }
        }

    }
}


