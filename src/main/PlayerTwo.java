package main;
import java.util.Arrays;

public class PlayerTwo {

	public static void print_field(State s) {
		int[][] field = s.getField();
		for (int i=19;i>=0;i--){
			System.out.println(Arrays.toString(field[i]));
		}
		System.out.println("");
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s); // launch graphics
		int total_reward = 0;

		//Set static weights and childNum
		PieceNode.setChildNum(2);
		BitBoardCol.setWeights(new double[] {

				13.8346672058105469, // aggr. height
				4.8310699462890625, // complete lines
				22.56073760986328,   // holes
				40.801902770996094,  // col transitions
				46.64051818847656,   // row transitions
				18.464271545410156,  // landing height
				7.776729583740234,   // top parity
				32.24385070800781,   // top variety
				17.081558227539062,  // mini max top
				2.219821929931641,   // side bump	
				0.000000000000000,   // bumpiness

		});

		//initialize root node's parent bn
		//BitBoardCol.initPieceBits();
		BitBoardCol bb = new BitBoardCol(new int[10], new int[10] );

		BoardNode bn = new BoardNode(bb);

		//initialize root
		//root always points to a PieceNode
		PieceNode root = new PieceNode(bn, s.nextPiece);

		//Initialize depth
		//every call to rootExpand expands the tree by 1 layer
		//uncomment to increase depth
		//last call must be rootExpandAndUpdate

		root.rootExpand();
		root.rootExpand();
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

			s.draw();
			s.drawNext(0,0);

			total_reward+= root.bestNode.bb.getReward();
			root = root.setRootToBest(s.nextPiece);
			//root.rootExpand();
			root.rootExpandAndUpdate();
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		//return s.getRowsCleared or total_reward for training simulations
	}
}