package main;
import java.util.Arrays;
import java.util.Random;



public class GeneticTrainer {
    
    private PartialPlayerTwo player = new PartialPlayerTwo();
    
    private final static double MAX_WEIGHT_VALUE = 100;
    private final static int NUM_HEURISTICS = 7;
    private final static int PERCENT_BEST_SELECT = 10;
    
    private final static int INIT_NUM_OF_TESTS = 100;
    private final static int MAX_NUM_NEW_TESTS = INIT_NUM_OF_TESTS / 10 * 3;
    
    private final static int PERCENT_MUTATION_CHANCE = 10;
    private final static double MAX_MUTATION = 0.2;
    
    private double[][] tests = new double[INIT_NUM_OF_TESTS][NUM_HEURISTICS];
    private Score[] scores = new Score[INIT_NUM_OF_TESTS + MAX_NUM_NEW_TESTS];
    private Score[] newScores = new Score[MAX_NUM_NEW_TESTS];
    
    private int numTests;
    private int numNewTests;
    private Random random;
    
    public GeneticTrainer() {
        numTests = INIT_NUM_OF_TESTS;
        numNewTests = 0;
        random = new Random();
    }
    //initialize basic weights spread across all possible weight values
    public void initializeWeights() {
        for (int i = 0; i < INIT_NUM_OF_TESTS; i += (2 * MAX_WEIGHT_VALUE / INIT_NUM_OF_TESTS)) {
            //adjust for negative values
            double weight = i - MAX_WEIGHT_VALUE;
            tests[i] = new double[] { weight, weight, weight, weight, weight, weight, weight};
        }
    }
    
    //get the scores for the weights
    public void runGame() {
        for (int i = 0; i < numTests; i++) {
            scores[i] = new Score(player.playGame(tests[i]), i);
        }
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
        
        /*public int getIndex() {
            return index;
        }*/
        
        public String toString() {
            return score + ", " + index;
        }
        
    }
    
    //select best % of the weights for reproduction - not v sophisticated
    public void truncationSelection() {
        Arrays.sort(scores);
        for (int i = 1; i <= numTests/PERCENT_BEST_SELECT; i++) {
            System.out.println(scores[INIT_NUM_OF_TESTS - i]);
        }
    }
    
    // crossovers two sets of tests at one single point, then mutate each new one
    public void crossoverAndMutate(double[] test1, double[] test2) {
        double[] newTest1 = new double[NUM_HEURISTICS];
        double[] newTest2 = new double[NUM_HEURISTICS];
        
        //finds point at which to crossover between index [1 to NUM_HEURISTICS - 1]
        int crossoverIndex = random.nextInt(NUM_HEURISTICS - 1) + 1; 
        
        for (int i = 0; i < NUM_HEURISTICS; i++) {
            if (i < crossoverIndex) {
                newTest1[i] = mutate(test1[i]);
                newTest2[i] = mutate(test2[i]);
            } else {
                newTest1[i] = mutate(test2[i]);
                newTest2[i] = mutate(test1[i]);
            }
        }
        
    }
    
    public double mutate(double value) {
        if (random.nextInt(100) < PERCENT_MUTATION_CHANCE) {
            //randomly add a value within +-MAX_MUTATION
            value += (random.nextDouble() * MAX_MUTATION * 2) - MAX_MUTATION;
        }
        return value;
    }
    
    
    
    public static void main(String[] args) {
        GeneticTrainer geneticTrainer = new GeneticTrainer();
        //geneticTrainer.initializeWeights();
        //geneticTrainer.runGame();
        //geneticTrainer.truncationSelection();
        double[] test1 = new double[] {1, 1, 1, 1, 1, 1, 1};
        double[] test2 = new double[] {2, 2, 2, 2, 2, 2, 2};
        
        
        
    }
}
