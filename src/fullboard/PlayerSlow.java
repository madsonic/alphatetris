package fullboard;

import main.State;

public class PlayerSlow {

	public static final double[] WEIGHTS = {
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
	
	public static void main(String[] args) {
		State s = new State();
		//new TFrame(s); // launch graphics
		int total_reward = 0;

		//Set static weights and childNum
		PieceNodeSlow.setChildNum(4);

		//initialize root node's parent bn
		//BitBoardCol.initPieceBits();
		BitBoardFullArray bb = new BitBoardFullArray();

		BoardNodeSlow bn = new BoardNodeSlow(bb);

		//initialize root
		//root always points to a PieceNode
		PieceNodeSlow root = new PieceNodeSlow(bn, s.nextPiece);

		//Initialize depth
		//every call to rootExpand expands the tree by 1 layer
		//uncomment to increase depth
		//last call must be rootExpandAndUpdate

		root.rootExpand();
		//root.rootExpand();
//		root.rootExpand();
		//root.rootExpand();
		root.rootExpandAndUpdate();

		while(!s.hasLost()) {

			if (s.getTurnNumber()%1000 == 500) {
			System.out.println("Turn num: "+ s.getTurnNumber()
			+ "   Avg reward: " + total_reward/s.getTurnNumber()
			+ "   Lines cleared: " + s.getRowsCleared());
			}

			//update ExpectiMiniMax values
			//root.rootUpdate();
			s.makeMove(root.getBestMove());

			//s.draw();
			//s.drawNext(0,0);

			total_reward+= root.bestNode.bb.getReward();
			root = root.setRootToBest(s.nextPiece);
			//root.rootExpand();
			root.rootExpandAndUpdate();
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		//return s.getRowsCleared or total_reward for training simulations
	}
}