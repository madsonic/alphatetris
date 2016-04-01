package main;
import java.util.Arrays;



public class GeneticTrainer {
    
    private PartialPlayerTwo player = new PartialPlayerTwo();
    
    private final static int MAX_WEIGHT_VALUE = 1000;
    private final static int MAX_NUM_WEIGHTS = 100;
    private final static int NUM_HEURISTICS = 4;
    private final static int PERCENT_BEST_REPRODUCE = 10;
    private double[][] weights = new double[MAX_NUM_WEIGHTS][NUM_HEURISTICS];
    private Score[] scores = new Score[MAX_NUM_WEIGHTS];
    
    //initialize basic weights spread across the max weight values
    public void initializeWeights() {
        for (int i = 0; i < MAX_NUM_WEIGHTS; i += (MAX_WEIGHT_VALUE / MAX_NUM_WEIGHTS)) {
            weights[i] = new double[] { i, i, i, i};
        }
    }
    
    //get the scores for the weights
    public void runGame() {
        for (int i = 0; i < MAX_NUM_WEIGHTS; i++) {
            scores[i] = new Score(player.playGame(weights[i]), i);
        }
        Arrays.sort(scores);
    }
    
    private class Score implements Comparable<Score>{
        int score;
        int index;
        
        public Score(int score, int index) {
            this.score = score;
            this.index = index;
        }
        
        @Override
        public int compareTo(Score o) {
            if (score > o.score) {
                return 1;
            } else if (score < o.score) {
                return -1;
            } else {
                return 0;
            }
        }
        
        public int getIndex() {
            return index;
        }
        
        public String toString() {
            return score + ", " + index;
        }
        
    }
    
    //select best % of the weights for reproduction
    public void produceOffspring() {
        for (int i = 1; i <= MAX_NUM_WEIGHTS/PERCENT_BEST_REPRODUCE; i++) {
            System.out.println(scores[MAX_NUM_WEIGHTS - i]);
        }
    }
    
    public static void main(String[] args) {
        GeneticTrainer meghanTrainor = new GeneticTrainer();
        meghanTrainor.initializeWeights();
        meghanTrainor.runGame();
        meghanTrainor.produceOffspring();
        
        
    }
}
