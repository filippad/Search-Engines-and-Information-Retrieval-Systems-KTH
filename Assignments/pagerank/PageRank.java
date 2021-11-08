import java.util.*;
import java.io.*;

public class PageRank {

    /**
     * Maximal number of documents. We're assuming here that we
     * don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     * Mapping from document names to document numbers.
     */
    HashMap<String, Integer> docNumber = new HashMap<>();

    /**
     * Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     * A memory-efficient representation of the transition matrix.
     * The outlinks are represented as a HashMap, whose keys are
     * the numbers of the documents linked from.<p>
     * <p>
     * The value corresponding to key i is a HashMap whose keys are
     * all the numbers of documents j that i links to.<p>
     * <p>
     * If there are no outlinks from i, then the value corresponding
     * key i is null.
     */
    HashMap<Integer, HashMap<Integer, Boolean>> link = new HashMap<>();

    /**
     * The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     * List of all documents that link to a specific one
     */
    ArrayList<ArrayList<Integer>> inlinksList = new ArrayList<>();

    /**
     * The probability that the surfer will be bored, stop
     * following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     * Convergence criterion: Transition probabilities do not
     * change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0000196;

       
    /* --------------------------------------------- */


    public PageRank(String filename) {
        for (int i = 0; i < MAX_NUMBER_OF_DOCS; i++) {
            inlinksList.add(new ArrayList<>());
        }

        int noOfDocs = readDocs(filename);
        iterate(noOfDocs, 1000);
    }


    /* --------------------------------------------- */

    private void writeFile (String fileName, double[] pr, int[] docIDs,
                            int length) throws IOException {
        FileOutputStream fout = new FileOutputStream(fileName);
        for (int i = 0; i < length; i++) {
            int docNumber = docIDs[i];
            String name = docName[docNumber];
            String toWrite = name + ": " + pr[i] + "\n";
            fout.write(toWrite.getBytes());
        }

        fout.close();
    }


    /**
     * Reads the documents and fills the data structures.
     *
     * @return the number of documents read.
     */
    int readDocs(String filename) {
        int fileIndex = 0;
        try {
            System.err.print("Reading file... ");
            System.out.println();
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
                int index = line.indexOf(";");
                String title = line.substring(0, index);
                Integer fromdoc = docNumber.get(title);
                //  Have we seen this document before?
                if (fromdoc == null) {
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put(title, fromdoc);
                    docName[fromdoc] = title;
                }

                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");
                while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get(otherTitle);
                    if (otherDoc == null) {
                        // This is a previously unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put(otherTitle, otherDoc);
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if (link.get(fromdoc) == null) {
                        link.put(fromdoc, new HashMap<>());
                    }
                    if (link.get(fromdoc).get(otherDoc) == null) {
                        link.get(fromdoc).put(otherDoc, true);
                        out[fromdoc]++;
                        inlinksList.get(otherDoc).add(fromdoc);
                    }
                }
            }

            if (fileIndex >= MAX_NUMBER_OF_DOCS) {
                System.err.print("stopped reading since documents table is full. ");
            } else {
                System.err.print("done. ");
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("Error reading file " + filename);
        }
        System.err.println("Read " + fileIndex + " number of documents");
        return fileIndex;
    }


    /* --------------------------------------------- */

    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     */
    void iterate(int numberOfDocs, int maxIterations) {

        long startTime = System.nanoTime();

        double[] probs = powerIteration(numberOfDocs, maxIterations);

        System.out.println("Power interation : " +
                (System.nanoTime() - startTime) / 1000000000 + " seconds");

        int[] docNums = new int[numberOfDocs];
        Arrays.setAll(docNums, i -> i);
        sort(probs, docNums, 0 , probs.length - 1);

        try {
            writeFile("my_top_30.txt", probs, docNums, 30);
            writeFile("pageRanks.txt", probs, docNums, numberOfDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<Integer> getNoOutLinks(int numberOfDocs) {
        ArrayList<Integer> noOutLinks = new ArrayList<>();
        for (int i = 0; i < numberOfDocs; i++) {
            if(out[i] == 0){
                noOutLinks.add(i);
            }
        }
        return noOutLinks;
    }

    double[] powerIteration(int numberOfDocs, int maxIterations) {
        ArrayList<Integer> noOutLinks = getNoOutLinks(numberOfDocs);

        noOutLinks.add(4);
        double[] probs = new double[numberOfDocs];
        probs[0] = 1.0;
        double jTerm = BORED/numberOfDocs;
        double c = 1-BORED;
        double delta;
        int iteration = 0;
        double sumJTerms;

        while (++iteration <= maxIterations) {
            delta = 0;
            sumJTerms = 0;
            double[] newRow = new double[numberOfDocs];

            // Step 1: calculate xi * jTerm;
            for (int i = 0; i < numberOfDocs; i++) {
                newRow[i] = probs[i] * jTerm;
                sumJTerms += newRow[i];
            }

            double sumAll = 0;
            for (int i = 0; i < numberOfDocs; i++) {

                // Jump to the corresponding column with all inlinks
                ArrayList<Integer> inlinks = inlinksList.get(i);
                double sum = 0;

                for (Integer doc : inlinks) {
                    sum += probs[doc] / out[doc];
                }

                for (Integer doc: noOutLinks) {
                    sum += probs[doc] /numberOfDocs;
                }

                newRow[i] = sum * c + sumJTerms;
                sumAll += newRow[i];
                delta += Math.abs(probs[i] - newRow[i]);
            }

            // Normalize the pageranks
            for (int i = 0; i < numberOfDocs; i++) {
                probs[i] = newRow[i]/sumAll;
            }

            System.err.println("iteration: " + iteration + ", delta: " + delta);

            if (delta < EPSILON) {
                break;
            }
        }

        return probs;
    }

    int partition(double pageRanks[], int[] docNums, int low, int high) {
        double pivot = pageRanks[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            // If current element is smaller than the pivot
            if (pageRanks[j] > pivot) {
                i++;

                // swap pageRanks[i] and pageRanks[j]
                double temp = pageRanks[i];
                pageRanks[i] = pageRanks[j];
                pageRanks[j] = temp;

                // swap doc numbers
                int t = docNums[i];
                docNums[i] = docNums[j];
                docNums[j] = t;
            }
        }

        // swap pageRanks[i+1] and pageRanks[high] (or pivot)
        double temp = pageRanks[i + 1];
        pageRanks[i + 1] = pageRanks[high];
        pageRanks[high] = temp;

        // swap doc numbers
        int t = docNums[i+1];
        docNums[i+1] = docNums[high];
        docNums[high] = t;

        return i + 1;
    }

    void sort(double arr[], int[] docNums, int low, int high) {
        if (low < high) {
            /* pi is partitioning index, arr[pi] is
              now at right place */
            int pi = partition(arr, docNums, low, high);

            // Recursively sort elements before
            // partition and after partition
            sort(arr, docNums, low, pi - 1);
            sort(arr, docNums, pi + 1, high);
        }
    }


    /* --------------------------------------------- */

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please give the name of the link file");
        } else {
            new PageRank(args[0]);
        }
    }
}
