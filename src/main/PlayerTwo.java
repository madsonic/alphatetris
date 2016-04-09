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
				51.60664832781422, // aggr. height
				14.412817176215912, // complete lines
				20.815265867296894, // bumpiness
				75.98173950628988,  // holes
				12.088312420164678, // col transitions
				-31.89757332137397, // row transitions
				47.513572649171685  // landing height
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