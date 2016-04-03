package pso;

import net.sourceforge.jswarm_pso.FitnessFunction;

public class MyFitnessFunction extends FitnessFunction {

	@Override
	public double evaluate(double[] position) {
		GameSimulation game = new GameSimulation();
		return game.start(position);
	}

}
