/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import ir.Query.*;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    HashMap<Integer, HashMap<String, Double>> tfidfMap = new HashMap<>();


    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;

    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings postingsMap representing the result of the query.
     */
    public PostingsList search(Query query, QueryType queryType, RankingType rankingType) {

        ArrayList<QueryTerm> queryTerms = query.queryterm;

        if (queryType == QueryType.INTERSECTION_QUERY || queryType == QueryType.PHRASE_QUERY) {
            ArrayList<String> oriTerms = new ArrayList<>();

            for (QueryTerm term : query.queryterm) {
                oriTerms.add(term.term);
            }

            if (queryTerms.size() == 1)
                return index.getPostings(queryTerms.get(0).term);
            else {

                // Sort terms in increasing order of their frequencies
                int numTerms = queryTerms.size();
                boolean ok = quickSort(queryTerms, 0, numTerms - 1);

                if (!ok)
                    return null;

                // Use the shortest posting postingsMap to find intersection
                String init = queryTerms.get(0).term;

                if (index.getPostings(init) == null)
                    return null;

                ArrayList<Integer> initList = index.getPostings(init).getSortedList();
                ArrayList<Integer> result = deepCopy(initList);

                intersect(result, queryTerms, queryType, oriTerms);
                HashMap<Integer, PostingsEntry> listRe = new HashMap<>();

                // Build the matching docs
                try {
                    HashMap<Integer, PostingsEntry> initPostings = index.getPostings(init).postingsMap;
                    result.forEach(docID -> listRe.put(docID, initPostings.get(docID)));

                } catch (NullPointerException e) {
                    return null;
                }

                return new PostingsList(result, listRe);
            }
        } else if (queryType == QueryType.RANKED_QUERY) {
            HashSet<Integer>  matchingDocIDs = new HashSet<>();
            TFIDF tfidfCalculator = new TFIDF(index);

            long start = System.nanoTime();
            // Step 1: Find matching docs (docs that include at least one of the search terms)
           queryTerms.forEach(queryTerm -> {
                String term = queryTerm.term;
                HashMap<Integer, PostingsEntry> currentMap = index.getPostings(term).postingsMap;

                currentMap.keySet().forEach(docID -> {
                    if (!matchingDocIDs.contains(docID))
                        matchingDocIDs.add(docID);
                });
            });

            // Step 2: Calculate the score for each matching document
            start = System.nanoTime();
            List<PostingsEntry> docsList = new ArrayList<>();

            if (rankingType == RankingType.PAGERANK) {
                matchingDocIDs.forEach(docID -> {
                    double score = index.pageRanks.get(docID);
                    PostingsEntry pEntry = new PostingsEntry(docID, score);
                    docsList.add(pEntry);
                });

            } else {
                double iiWeight;
                if (rankingType == RankingType.TF_IDF)
                    iiWeight = 1;
                else
                    iiWeight = 0.4;
                double prWeight = 1 - iiWeight;

                for (Integer docID : matchingDocIDs) {
                    double score = 0;
                    PostingsEntry pEntry = new PostingsEntry(docID, score);

                    for (QueryTerm queryTerm : queryTerms) {
                        String term = queryTerm.term;

                        HashMap<String, Double> termsInDoc = tfidfMap.get(docID);
                        Double termDocFrequency;
                        if (termsInDoc != null) {
                            termDocFrequency = termsInDoc.get(term);

                            if (termDocFrequency == null) {
                                termDocFrequency = tfidfCalculator.getTFIDF(term, docID) / index.docLengths.get(docID);
                                termsInDoc.put(term, termDocFrequency);
                            }
                        } else {
                            termsInDoc = new HashMap<>();
                            termDocFrequency = tfidfCalculator.getTFIDF(term, docID) / index.docLengths.get(docID);
                            termsInDoc.put(term, termDocFrequency);
                        }

                        score += (termDocFrequency * queryTerm.weight);
                    }

                    double pr;
                    try {
                        pr = index.pageRanks.get(docID);
                    } catch (NullPointerException e) {
                        pr = 1;
                    }
                    pEntry.setScore(iiWeight * score + prWeight * pr);
                    docsList.add(pEntry);
                }
            }

//            System.err.println("Search step 2: " + ((System.nanoTime() - start) / 1e9));

            // Step 3: Build up the result
            start = System.nanoTime();
            Collections.sort(docsList);
            ArrayList<Integer> docIDs = new ArrayList<>();
            HashMap<Integer,PostingsEntry> map = new HashMap<>();
            for (PostingsEntry entry: docsList) {
                docIDs.add(entry.docID);
                map.put(entry.docID, entry);
            }
//            System.err.println("Search step 3: " + ((System.nanoTime() - start) / 1e9));

            return new PostingsList(docIDs, map);

        } else throw new IllegalArgumentException("Invalid Query Type");
    }

    private void intersect(ArrayList<Integer> pListToCompare, ArrayList<QueryTerm> queryTerms,
                           QueryType queryType, ArrayList<String> oriTerms) {
        int currentIndex = 1;

        if (queryType == QueryType.INTERSECTION_QUERY) {
            while (pListToCompare.size() != 0 && currentIndex < queryTerms.size()) {
                PostingsList currentList = index.getPostings(queryTerms.get(currentIndex).term);
                pListToCompare = intersectList(pListToCompare, currentList.postingsMap);
                currentIndex++;
            }
        } else if (queryType == QueryType.PHRASE_QUERY) {
            while (pListToCompare.size() != 0 && currentIndex < queryTerms.size()) {
                PostingsList currentList = index.getPostings(queryTerms.get(currentIndex).term);
                pListToCompare = intersectList(pListToCompare, currentList.postingsMap);
                currentIndex++;
            }

            ArrayList<Integer> actualOrder = new ArrayList<>();

            for (int i = 0; i < queryTerms.size(); i++) {
                String term = queryTerms.get(i).term;

                for (int j = 0; j < oriTerms.size(); j++) {
                    String oriTerm = oriTerms.get(j);
                    if (term.equals(oriTerm) && !actualOrder.contains(j)) {
                        actualOrder.add(j);
                        break;
                    }
                }
            }

            String startTerm = queryTerms.get(0).term;
            HashMap<Integer, PostingsEntry> postingMap = index.getPostings(startTerm).postingsMap;

            // For each doc check if there is a valid set of offsets for the positional terms in the query
            int i = 0;
            while (i < pListToCompare.size()) {
                int docID = pListToCompare.get(i);
                ArrayList<Integer> startOffsets = postingMap.get(docID).getOffsets();
                boolean isDocValid = false;

                for (Integer offset: startOffsets) {
                    int currentPos = offset;
                    boolean isOffsetOk = true;

                    for (int j = 1; j < queryTerms.size(); j++) {
                        String thisTerm = queryTerms.get(j).term;
                        ArrayList<Integer> currentOffsets = index.getPostings(thisTerm).postingsMap.get(docID).getOffsets();

                        int dist = actualOrder.get(j) - actualOrder.get(j-1);
                        currentPos += dist;

                        if (!currentOffsets.contains(currentPos)) {
                            isOffsetOk = false;
                            break;
                        }
                    }

                    if (isOffsetOk) {
                        isDocValid = true;
                        break;
                    }
                }

                if (isDocValid)
                    i++;
                else pListToCompare.remove(i);
            }
        }
    }

    private ArrayList<Integer> intersectList (ArrayList<Integer> pList1, HashMap<Integer,PostingsEntry> pList2) {
        int currentIndex = 0;

        while (currentIndex < pList1.size()) {
            int docID = pList1.get(currentIndex);
            if (pList2.get(docID) == null) {
                pList1.remove(currentIndex);
            } else {
                currentIndex++;
            }
        }

        return pList1;
    }

    private int getFrequency (ArrayList<Query.QueryTerm> terms, int i) {
        String term = terms.get(i).term;
        PostingsList pl = index.getPostings(term);

        if (pl == null)
            return -1;
        return pl.size();
    }

    private int partition(ArrayList<Query.QueryTerm> terms, int low, int high)
    {
        int pivot = getFrequency(terms, high);

        if (pivot == -1)
            return -1;

        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++) {

            // If current element is smaller than the pivot
            if (getFrequency(terms, j) < pivot) {

                i++;
                Collections.swap(terms, i, j);
            }
        }

        Collections.swap(terms, i+1, high);

        return i+1;
    }

    private boolean quickSort(ArrayList<Query.QueryTerm> terms, int low, int high) {
        if (low < high)
        {
            int pi = partition (terms, low, high);
            if (pi == -1)
                return false;

            quickSort(terms, low, pi-1);
            quickSort(terms, pi+1, high);
        }

        return true;
    }

    private static ArrayList<Integer> deepCopy(ArrayList<Integer> toCopy) {
        ArrayList<Integer> result = new ArrayList<>();
        toCopy.forEach(i -> result.add(i));
        return result;
    }

}