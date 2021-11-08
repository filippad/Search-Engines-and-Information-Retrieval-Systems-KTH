package ir;

public class TFIDF {
    private Index index;

    public TFIDF(Index index) {
        this.index = index;
    }

    /**
     * Get normalised tf-idf for certain term and the respective document
     * @param term  the term to calculate tf-idf
     * @param docID ID of the document
     * @return tf-idf
     */
    public double getTFIDF(String term, int docID) {
        PostingsList pl = index.getPostings(term);
        double docFrequency = pl.size();
        double n = index.docNames.size();
        double idf = Math.log10(n / docFrequency);

        PostingsEntry currentEntry = pl.postingsMap.get(docID);
        if (currentEntry == null)
            return 0;

        double tf = currentEntry.getTermFrequency();

        return tf * idf;
    }

    double getTF (String term, int docID) {
        PostingsList pl = index.getPostings(term);
        PostingsEntry currentEntry = pl.postingsMap.get(docID);
        if (currentEntry == null)
            return 0;

        return currentEntry.getTermFrequency();
    }

    double getIDF(String term) {
        PostingsList pl = index.getPostings(term);
        double docFrequency = pl.size();
        double n = index.docNames.size();

        System.out.println(term + ": " + Math.log (n/docFrequency));
        return Math.log (n/docFrequency);
    }

}
