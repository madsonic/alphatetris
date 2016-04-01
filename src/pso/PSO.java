package pso;

import java.util.Arrays;

import net.sourceforge.jswarm_pso.Swarm;

public class PSO {
	static Integer maxPosBound = Integer.MAX_VALUE;
	static Integer stageBound = 5;
	static Integer particleOffset = 0;

	public static void main(String[] args) {
		Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES + particleOffset,
				new MyParticle(),
				new MyFitnessFunction());
		swarm.setMaxPosition(Math.random() * maxPosBound);
		swarm.setMinPosition(Math.random() * -maxPosBound);
		swarm.setGlobalIncrement(1.5);
		swarm.setParticleIncrement(0.9);
		swarm.setInertia(2.0);
		for( int i = 0; i < stageBound; i++ ){
			swarm.evolve();
			System.out.println("Stage: " + i);
		}
		
		String res = swarm.getBestFitness()+":"+Arrays.toString(swarm.getBestPosition());
		System.out.println(res);
	}


}
