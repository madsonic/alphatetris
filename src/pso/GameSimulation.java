package pso;

import main.BitBoardCol;
import main.BoardNode;
import main.PieceNode;
import main.State;

public class GameSimulation {
	
	static final int MAX_MOVES = 100000;
	
	public GameSimulation() {	
	}
	
	public int start(double[] weights) {
        State s = new State();

        // initialize root node's parent bn
        BitBoardCol.initPieceBits();
        BitBoardCol bb = new BitBoardCol(new int[10], s.getTop());
        BoardNode bn = new BoardNode(null, bb, new int[] { 1, 1 });
        bb.setWeights(weights);
        
        // initialize root
        // root always points to a PieceNode
        PieceNode.setChildNum(2);
        PieceNode root = new PieceNode(bn, s.nextPiece);

        // Initialize depth
        // every call to rootExpand expands the tree by 1 layer
        // uncomment to increase depth

        root.rootExpand();
        // root.rootExpand();
        // root.rootExpand();
        // root.rootExpand();

        while (!s.hasLost() && s.getTurnNumber() < MAX_MOVES) {
            root.rootExpand();

            // update ExpectiMiniMax values
            root.rootUpdate();

            s.makeMove(root.getBestMove());
            // print_field(s);
            root = root.setRootToBest(s.nextPiece);
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
        return s.getRowsCleared();
	}
}
