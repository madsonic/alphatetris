package main;
import java.util.Arrays;

public class PlayerTwo{
	public static void print_field(State s) {
		int[][] field = s.getField();
		for (int i=19;i>=0;i--){
			System.out.println(Arrays.toString(field[i]));
		}
		System.out.println("");
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		int total_score = 0;
		
		//initialize root node's parent bn
		BitBoardCol.initPieceBits();
		BitBoard bb = new BitBoardCol(new int[10],s.getTop());
		bb.setWeights(new double[] { -51.60664832781422, 14.412817176215912,
				20.815265867296894, 75.98173950628988,
				12.088312420164678, -31.89757332137397, 47.513572649171685 });
		BoardNode bn = new BoardNode(null, bb , new int[] {1,1});
		
		//initialize root
		//root always points to a PieceNode
		PieceNode.setChildNum(2);
		PieceNode root = new PieceNode(bn, s.nextPiece);
		
		//Initialize depth
		//every call to rootExpand expands the tree by 1 layer
		//uncomment to increase depth

		root.rootExpand();
		//root.rootExpand();
		//root.rootExpand();
		//root.rootExpand();

		while(!s.hasLost()) {
            if (s.getTurnNumber()%2000 == 1) {
				System.out.println(s.getTurnNumber() + ": "+total_score/s.getTurnNumber());
            }
			root.rootExpand();
			
			//update ExpectiMiniMax values
			root.rootUpdate();
			
			s.makeMove(root.getBestMove());
			//print_field(s);
			
			s.draw();
			s.drawNext(0,0);

			total_score += root.bestNode.BoardState.getValue();
			root = root.setRootToBest(s.nextPiece);

//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
