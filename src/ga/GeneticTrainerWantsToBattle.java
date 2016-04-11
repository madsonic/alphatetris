package ga;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;

public class GeneticTrainerWantsToBattle {
    private static final int POPULATION_SIZE = 1000;
    private static final int NUM_HEURISTICS = 11;
    // can set this to negative but so far the best always gives positive values so might as well constrain
    private static final double WEIGHT_VALUE_MIN = 0;
    private static final double WEIGHT_VALUE_MAX = 10;
    private static final int MAX_ALLOWED_EVOLUTIONS = 20;

    public static void main(String[] args) throws Exception {
        Configuration conf = new DefaultConfiguration();

        // seed with sample chromosome
        Gene[] sampleGenes = new Gene[NUM_HEURISTICS];
        for (int i = 0; i < NUM_HEURISTICS; i++) {
            sampleGenes[i] = new DoubleGene(conf, WEIGHT_VALUE_MIN,
                    WEIGHT_VALUE_MAX);
        }
        Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);
        conf.setSampleChromosome(sampleChromosome);

        TetrisFitnessFunction fitness = new TetrisFitnessFunction();
        conf.setFitnessFunction(fitness);
        
        conf.setPopulationSize(POPULATION_SIZE);

        // randomly initialize population
        Genotype population = Genotype.randomInitialGenotype(conf);

        // actual training begins here
        for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {
            population.evolve();
            System.out.println("Evolution: " + i);
        }

        IChromosome bestSolution = population.getFittestChromosome();

        fitness.printHeuristics(bestSolution);

    }
}
