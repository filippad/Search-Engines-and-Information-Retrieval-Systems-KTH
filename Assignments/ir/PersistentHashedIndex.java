/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, KTH, 2018
 */  

package ir;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 *   Implements an inverted index as a hashtable on disk.
 *   
 *   Both the words (the dictionary) and the data (the postings postingsMap) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks. 
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The dictionary file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;

    static final int ENTRYSIZE = 16;

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<>();

    private List<Long> usedHashes = new ArrayList<>();

    // ===================================================================

    /**
     *
     *   A helper class representing one entry in the dictionary hashtable.
     *
     */
    public class Entry {
        long dataPtr;
        int size;
        Character firstChar;
        Character secondChar;

        /**
         *
         * @param dataPtr the position of the pointer where the term's posting is stored in data file
         * @param size the size (in bytes) of the String that is stored in the data file
         */
        Entry (long dataPtr, int size, Character firstChar, Character secondChar) {
            this.dataPtr = dataPtr;
            this.size = size;
            this.firstChar = firstChar;
            this.secondChar = secondChar;
        }

    }


    // ==================================================================

    
    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don'getIDF exist, they will be created.
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
            System.err.print("Can not find file: ");
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */ 
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek(ptr);
            byte[] data = dataString.getBytes();
            dataFile.write(data);
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */ 
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file. 
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr) throws IOException {
        dictionaryFile.seek(ptr);
        dictionaryFile.writeChar(entry.firstChar);
        dictionaryFile.writeChar(entry.secondChar);
        dictionaryFile.writeLong(entry.dataPtr);
        dictionaryFile.writeInt(entry.size);
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     * @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry(long ptr, String token) throws IOException {
        int skip = 3;
        boolean doneReading = false;

        while (!doneReading) {
            dictionaryFile.seek(ptr);
            Character firstChar = dictionaryFile.readChar();

            if (firstChar == token.charAt(0)) {
                Character secondChar = dictionaryFile.readChar();
                if ((token.length() > 1 && secondChar == token.charAt(1)) ||
                        (token.length() == 1 && secondChar =='*')) {

                    long dataPtr = dictionaryFile.readLong();
                    int size = dictionaryFile.readInt();
                    return new Entry(dataPtr, size, firstChar, secondChar);
                }
            }

            long prelHash = ptr / ENTRYSIZE;
            prelHash = (prelHash + skip) % TABLESIZE;
            ptr = prelHash * ENTRYSIZE;

            dictionaryFile.seek(ptr);
            Character tryChar = dictionaryFile.readChar();
            if (tryChar == ' ' || tryChar == '\0')
                doneReading = true;
        }

        return null;
    }

    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for (Map.Entry<Integer,String> entry : docNames.entrySet()) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write(docInfoEntry.getBytes());
        }
        fout.close();
    }

    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try (BufferedReader br = new BufferedReader(freader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put(new Integer(data[0]), data[1]);
                docLengths.put(new Integer(data[0]), new Integer(data[2]));
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings postingsMap
            for (String term: index.keySet()) {

                // write the posting list into the data file
                long dataPtr = dataFile.getFilePointer();
                PostingsList pl = index.get(term);
                String sPL = pl.encode();
                int size = writeData(sPL, dataPtr);

                // write the dataPtr to the dictionary file
                long dictPtr = getHashPosition (term);

                // handle collision
                if (isCollision(dictPtr)) {
                    collisions++;
                    dictPtr = rehash(dictPtr);
                }

                usedHashes.add(dictPtr);

                Entry e;
                if (term.length() > 1)
                    e = new Entry(dataPtr, size, term.charAt(0), term.charAt(1));
                else
                    e = new Entry(dataPtr, size, term.charAt(0), '*');

                writeEntry(e, dictPtr);

            }

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }

    private long rehash(long dictPtr) throws IOException {
        int skip = 3;
        long prelHash = dictPtr / ENTRYSIZE;

        do {
            prelHash = (prelHash + skip) % TABLESIZE;
            dictPtr = prelHash * ENTRYSIZE;

        } while (isCollision(dictPtr));

        return dictPtr;
    }

    private long getHashPosition(String term) {
        int prelHash = Math.abs(term.hashCode());

        return (prelHash % TABLESIZE) * ENTRYSIZE;
    }

    private boolean isCollision(long ptr) throws IOException {
        return usedHashes.contains(ptr);
    }

    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings(String token ) {

        PostingsList postingsList = index.get(token);

        if (postingsList == null) {
            try {
                long hashedToken = getHashPosition(token);
                Entry entry = readEntry(hashedToken, token);

                if (entry != null) {
                    String encodedPL = readData(entry.dataPtr, entry.size);
                    postingsList = decodePostingList (encodedPL);
                    index.put(token, postingsList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return postingsList;
    }

    private PostingsList decodePostingList(String encodedPL) {

        HashMap<Integer, PostingsEntry> postingsMap = new HashMap<>();

        String[] entries = encodedPL.split("-");
        String lastEntry = entries[entries.length-1];
        entries[entries.length-1] = lastEntry.substring(0, lastEntry.length()-1);

        for (String e : entries) {
            String[] parts = e.split(";");

            int docID = Integer.parseInt(parts[0]);
            double score = 0.0;

            String[] stringOffsets = parts[1].split(",");
            ArrayList<Integer> offsets = new ArrayList<>();
            for (String stringOffset : stringOffsets) {
                int offset = Integer.parseInt(stringOffset);
                offsets.add(offset);
            }

            PostingsEntry pl = new PostingsEntry(docID, score, offsets);
            postingsMap.put(docID, pl);
        }

        return new PostingsList(postingsMap);

    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert(String token, int docID, int offset) {
        PostingsList pl = index.get(token);

        // If the token is a new entry
        if (pl == null) {
            // Create an empty PostingsList
            pl  = new PostingsList();
            index.put(token, pl);
        }

        pl.add(docID, offset);
    }


    /**
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();
        System.err.println( "done!" );
    }
}
