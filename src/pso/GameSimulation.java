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
        
        int total_reward = 0;

		//Set static weights and childNum
		BitBoardCol.setWeights(weights);
		PieceNode.setChildNum(2);
        
		//initialize root node's parent bn
		//BitBoardCol.initPieceBits();
		BitBoardCol bb = new BitBoardCol(new int[10], new int[10] );
		BoardNode bn = new BoardNode(bb);
		
		//initialize root
		//root always points to a PieceNode
		PieceNode root = new PieceNode(bn, s.nextPiece);

		//Initialize depth
		//every call to rootExpand expands the tree by 1 layer
		//uncomment root.rootExpand() to increase look-ahead by 1
		//last call must be rootExpandAndUpdate

		//root.rootExpand();
		//root.rootExpand();
		root.rootExpandAndUpdate();

        while (!s.hasLost() && s.getTurnNumber() < MAX_MOVES) {

            // update ExpectiMiniMax values
            //root.rootUpdate();
            s.makeMove(root.getBestMove());

            total_reward+= root.bestNode.bb.getReward();
            root = root.setRootToBest(s.nextPiece);
			//root.rootExpand();
			root.rootExpandAndUpdate();
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
        return s.getRowsCleared();
        //return total_reward;
	}
}
