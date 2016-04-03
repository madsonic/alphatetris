package pso;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.jswarm_pso.Swarm;
public class AlphaSwarm implements Runnable {
    static final Integer maxPosBound = Integer.MAX_VALUE;
    static final int stageBound = 1;
    static final int particleOffset = 0;

    static final double globalIncrement = 1.5;
    static final double particleIncrement = 0.9;
    static final double inertia = 2.0;

    private List<String> results;

    public AlphaSwarm(List<String> resultArray) {
        results = resultArray;
    }

    public void run() {
        Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES + particleOffset,
                new MyParticle(),
                new MyFitnessFunction());
        swarm.setMaxPosition(Math.random() * maxPosBound);
        swarm.setMinPosition(Math.random() * -maxPosBound);

        swarm.setGlobalIncrement(globalIncrement);
        swarm.setParticleIncrement(particleIncrement);
        swarm.setInertia(inertia);
        
        for( int i = 0; i < stageBound; i++ ){
            swarm.evolve();
            // System.out.println("Stage: " + i);
        }
        
        String res = swarm.getBestFitness()+":"+Arrays.toString(swarm.getBestPosition());
        results.add(res);
    }   
}