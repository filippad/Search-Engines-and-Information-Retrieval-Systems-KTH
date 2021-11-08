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

    /** Index from k-grams to postingsMap of term ids that contain the k-gram */
    HashMap<String,List<KGramPostingsEntry>> index = new HashMap<>();

    /** The ID of the last processed term */
    int lastTermID = -1;

    /** Number of symbols to form a K-gram */
    int K = 3;

    public KGramIndex(int k) {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can'getIDF be constructed for a negative K value");
            System.exit(1);
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
    List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
        if (p2 == null)
            return null;
        int size1 = p1.size();
        int size2 = p2.size();

        List<KGramPostingsEntry> result;
        List<KGramPostingsEntry> toCompare;
        if (size1 < size2) {
            result = p1;
            toCompare = p2;
        } else {
            result = p2;
            toCompare = p1;
        }

        HashSet<Integer> searchSet = listToSet(toCompare);
        int i = 0;
        while (i < result.size()) {
            KGramPostingsEntry kgEntry = result.get(i);
            if (!searchSet.contains(kgEntry.tokenID))
                result.remove(i);
            else i++;
        }

        return result;
    }

    private HashSet<Integer> listToSet (List<KGramPostingsEntry> list) {
        HashSet<Integer> result = new HashSet<>();
        list.forEach(pe -> result.add(pe.tokenID));
        return result;
    }


    /** Inserts all k-grams from a token into the index. */
    public void insert( String token ) {
        Integer id = getIDByTerm(token);

        // Only process word if it has not been processed before
        if (id == null) {

            // Step 1: Generate ID for the new word and saved into the id and term lists
            id = generateTermID();
            id2term.put(id, token);
            term2id.put(token, id);

            // Step 2: Insert start and end tokens
            token = "^" + token + "$";

            ArrayList<String> processed = new ArrayList<>();

            int i = 0;
            // Loop through the word
            while (i <= token.length() - K){

                // Calculate kgram from the current position
                String kGram = token.substring(i, i + K);

                // Only continue of the kGram has not been seen before
                // to avoid adding the word more than once into the result list
                if (!processed.contains(kGram)) {
                    List<KGramPostingsEntry> entryList = index.computeIfAbsent(kGram, k -> new ArrayList<>());
                    KGramPostingsEntry newEntry = new KGramPostingsEntry(id);
                    entryList.add(newEntry);
                    processed.add(kGram);
                }
                i++;
            }
        }
    }

    /** Get postings for the given k-gram */
    List<KGramPostingsEntry> getPostings(String kGram) {
        List<KGramPostingsEntry> result = index.get(kGram);
        return result;
    }

    /** Get id of a term */
    Integer getIDByTerm(String term) {
        return term2id.get(term);
    }

    /** Get a term by the given id */
    String getTermByID(Integer id) {
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

    @SuppressWarnings("Duplicates")
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
