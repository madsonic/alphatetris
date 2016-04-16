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
        PieceNode.setChildNum(2);

        // initialize root node's parent bn
        // BitBoardCol.initPieceBits();
        BitBoardCol bb = new BitBoardCol(new int[10], new int[10], weights);
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
//         root.rootExpand();
//         root.rootExpand();
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
        PieceNode.setChildNum(2);

        // initialize root node's parent bn
        // BitBoardCol.initPieceBits();
        BitBoardCol bb = new BitBoardCol(new int[10], new int[10], weights);
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
//             if (s.getTurnNumber()%2000 == 500) {
//	              printScore(s); 
//             }
            // update ExpectiMiniMax values
            // root.rootUpdate();
            s.makeMove(root.getBestMove());
            
            // turn by turn graphics
            s.draw();
            s.drawNext(0,0);
            
            root = root.setRootToBest(s.nextPiece);
            // root.rootExpand();
            root.rootExpandAndUpdate();
        }
        
        System.out.println("I'm dead");
        printScore(s);
//        s.draw();
        return s.getRowsCleared();
    }
    
    // similar to play game but allows to play multiple rounds and print instead return score
    public void playGames(double[] weights, int rounds) {
    	for (int i = 0; i < rounds; i++) {
	    	State s = new State();
//        new TFrame(s);
	
	        // Set static weights and childNum
	        PieceNode.setChildNum(3);
	
	        // initialize root node's parent bn
	        // BitBoardCol.initPieceBits();
	        BitBoardCol bb = new BitBoardCol(new int[10], new int[10], weights);
	        BoardNode bn = new BoardNode(bb);
	
	        // initialize root
	        // root always points to a PieceNode
	        PieceNode root = new PieceNode(bn, s.nextPiece);
	
	        // Initialize depth
	        // every call to rootExpand expands the tree by 1 layer
	        // uncomment to increase depth
	        // last call must be rootExpandAndUpdate
	
	        root.rootExpand();
//	        root.rootExpand();
	        // root.rootExpand();
	        // root.rootExpand();
	        root.rootExpandAndUpdate();
        
            while (!s.hasLost()) {
               // update ExpectiMiniMax values
               // root.rootUpdate();
            	if (s.getTurnNumber() % 10000 == 0) {
            		printScore(s);
            	}
               s.makeMove(root.getBestMove());
               
               root = root.setRootToBest(s.nextPiece);
//                root.rootExpand();
               root.rootExpandAndUpdate();
//              s.draw();
//              s.drawNext(4, 0);
           }
           System.out.println("Round: " + i);
           printScore(s);
           s.draw();
		}
    }
    
    public static void main(String[] args) {
    	// play game
    	PartialPlayerTwo p = new PartialPlayerTwo();
    	
    	// best weights so far
    	double[] weights = {
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
    	p.playGames(weights, 1);
    }
    public static void printScore(State s) {
    	System.out.println("Turn num: "+ s.getTurnNumber() +"   Lines cleared: " + s.getRowsCleared());
    }
}
