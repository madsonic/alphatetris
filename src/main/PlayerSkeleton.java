package main;

import java.util.concurrent.ExecutionException;

public class PlayerSkeleton {

	final double[] WEIGHTS = {
			-1.4101417483877365,
			1.7211254382260732,
			-9.430143863717376,
			-9.994060706564685,
			-2.151838313833677,
			-0.44094887506903957,
			-0.1085005610847889,
			2.575604041318804,
			-0.782307878163081,
			-0.2962660424028918,
			-2.8036583031140183
	};

	final int BEAM_WIDTH = 2;
	final int LOOKAHEAD_DEPTH = 1;
	final boolean MULTITHREAD = false;
	final BeamSearchAgent agent = BeamSearchAgent.makeDefaultAgent(
			BEAM_WIDTH, LOOKAHEAD_DEPTH, WEIGHTS, MULTITHREAD
	);

	//implement this function to have a working system
	public int[] pickMove(State s, int[][] legalMoves) throws ExecutionException, InterruptedException {
		return agent.pickNextMove(s.getNextPiece());
	}
	
	public static void main(String[] args) throws Exception {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
