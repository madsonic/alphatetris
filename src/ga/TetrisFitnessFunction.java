package ga;

import main.PlayerTwo;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

@SuppressWarnings("serial")
public class TetrisFitnessFunction extends FitnessFunction{
    private static final int NUM_HEURISTICS = 10;
    
    PartialPlayerTwo player = new PartialPlayerTwo();
    PlayerTwo realPlayer = new PlayerTwo();
    
    private double[] convertChromosomeWeights(IChromosome weights) {
        double[] doubleWeights = new double[NUM_HEURISTICS];
        for (int i = 0; i < NUM_HEURISTICS; i++) {
            doubleWeights[i] = getGeneAtIndex(weights, i);
        }
        return doubleWeights;
        
    }
    
    public double getGeneAtIndex(IChromosome weights, int index) {
        return (double)weights.getGene(index).getAllele();
    }
    
    public void printHeuristics(IChromosome weights) {
        System.out.println("Final Heuristic Values: ");
        for (int i = 0; i < NUM_HEURISTICS; i++) {
            System.out.println(getGeneAtIndex(weights, i));
        }
        System.out.println("Number of Rows Cleared: ");
        System.out.println(player.playGame(convertChromosomeWeights(weights)));
    }
    
    @Override
    protected double evaluate(IChromosome weights) {
        return player.exploreMoves(convertChromosomeWeights(weights));
    }
    
    
}
