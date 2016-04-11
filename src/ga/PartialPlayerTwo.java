package ga;

import main.BitBoardCol;
import main.BoardNode;
import main.PieceNode;
import main.State;
import main.TFrame;

// exactly the same as Player Two but with option of early termination after x moves.

public class PartialPlayerTwo {
    public static final int MAX_MOVES = 10000;

    // returns fitness function
    public int exploreMoves(double[] weights) {
        State s = new State();

        int total_reward = 0;

        // Set static weights and childNum
        BitBoardCol.setWeights(weights);
        PieceNode.setChildNum(2);

        // initialize root node's parent bn
        // BitBoardCol.initPieceBits();
        BitBoardCol bb = new BitBoardCol(new int[10], new int[10]);
        BoardNode bn = new BoardNode(bb);

        // initialize root
        // root always points to a PieceNode
        PieceNode root = new PieceNode(bn, s.nextPiece);

        // Initialize depth
        // every call to rootExpand expands the tree by 1 layer
        // uncomment to increase depth
        // last call must be rootExpandAndUpdate

        root.rootExpand();
        // root.rootExpand();
        // root.rootExpand();
        // root.rootExpand();
        root.rootExpandAndUpdate();

        while (!s.hasLost() && s.getTurnNumber() < MAX_MOVES) {
            /*
             * if (s.getTurnNumber()%1000 == 500) {
             * System.out.println("Turn num: "+ s.getTurnNumber() +
             * "   Avg reward: " + total_reward/s.getTurnNumber() +
             * "   Lines cleared: " + s.getRowsCleared()); }
             */
            // update ExpectiMiniMax values
            // root.rootUpdate();
            s.makeMove(root.getBestMove());

            total_reward += root.bestNode.bb.getReward();
            root = root.setRootToBest(s.nextPiece);
            // root.rootExpand();
            root.rootExpandAndUpdate();
        }
        return total_reward;

    }
    
    // plays the game for real and returns rows cleared
    public int playGame(double[] weights) {
        State s = new State();
        new TFrame(s);

        // Set static weights and childNum
        BitBoardCol.setWeights(weights);
        PieceNode.setChildNum(2);

        // initialize root node's parent bn
        // BitBoardCol.initPieceBits();
        BitBoardCol bb = new BitBoardCol(new int[10], new int[10]);
        BoardNode bn = new BoardNode(bb);

        // initialize root
        // root always points to a PieceNode
        PieceNode root = new PieceNode(bn, s.nextPiece);

        // Initialize depth
        // every call to rootExpand expands the tree by 1 layer
        // uncomment to increase depth
        // last call must be rootExpandAndUpdate

        root.rootExpand();
        // root.rootExpand();
        // root.rootExpand();
        // root.rootExpand();
        root.rootExpandAndUpdate();

        while (!s.hasLost()) {
            
             if (s.getTurnNumber()%2000 == 500) {
	              System.out.println("Turn num: "+ s.getTurnNumber() +"   Lines cleared: " + s.getRowsCleared()); 
              }
            // update ExpectiMiniMax values
            // root.rootUpdate();
            s.makeMove(root.getBestMove());
            
            // turn by turn graphics
//            s.draw();
//            s.drawNext(0,0);
            
            root = root.setRootToBest(s.nextPiece);
            // root.rootExpand();
            root.rootExpandAndUpdate();
        }
        System.out.println("I'm dead");
        s.draw();
        return s.getRowsCleared();

    }
    
    public static void main(String[] args) {
    	// play game
    	PartialPlayerTwo p = new PartialPlayerTwo();
    	double[] weights = {1.550894525201647,
    	                    3.6393568332418647,
    	                    9.793561450312492,
    	                    8.146038155910917,
    	                    5.711970387950983,
    	                    2.1999248114171865,
    	                    0.9223982639211392,
    	                    4.220457292054116,
    	                    0.3770283420907572,
    	                    1.5238002012538043};
    	p.playGame(weights);
    }
}
