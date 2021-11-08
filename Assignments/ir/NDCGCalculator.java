package ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NDCGCalculator {
    static ArrayList<Integer> relevantScores = new ArrayList<>(
            Arrays.asList(1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 2, 0, 0, 0,
                    0, 0, 0, 0, 1,
                    1, 0, 0, 0, 3,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 1,
                    0, 1, 0, 0, 0));

    static ArrayList<Integer> feedbackScores = new ArrayList<>(
            Arrays.asList(1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0,
                    0, 1, 0, 0, 1,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 1, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 1,
                    0, 0, 0, 0, 1,
                    0, 0, 0, 0, 0));

    public static void main(String[] args) {
        double ndcg = calculateDCG(relevantScores)/calculateIDCG(relevantScores);
        System.out.println("Before relevant feedback: NDCG= " + ndcg);
        double fndcg = calculateDCG(feedbackScores)/calculateIDCG(feedbackScores);
        System.out.println("After relevant feedback: NDCG= " + fndcg);

    }

    private static double calculateDCG(ArrayList<Integer> scores) {
        double dcg = 0;
        for (int i = 0; i < scores.size(); i++) {
            Integer score = scores.get(i);
            if (score != 0)
                dcg += Math.log(2) * score / Math.log(i+2);

        }
        return dcg;
    }

    private static double calculateIDCG(ArrayList<Integer> scores) {
        double idcg = 0;

        Collections.sort(scores, Collections.reverseOrder());

        for (int i = 0; i < scores.size(); i++) {
            Integer score = scores.get(i);
            if (score != 0)
                idcg += Math.log(2) * score / Math.log(i+2);

        }
        return idcg;
    }
}
