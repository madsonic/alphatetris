package main;
import java.util.Arrays;

public class PlayerOne{
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
		
		//initialize root node's parent bn
		BitBoard bb = new BitBoardFullArray(s.getField(), s.getTop());
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

		while(!s.hasLost()) {
			root.rootExpand();
			
			//update ExpectiMiniMax values
			root.rootUpdate();
			
			s.makeMove(root.getBestMove());
			//print_field(s);
			
			s.draw();
			s.drawNext(0,0);
			
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
