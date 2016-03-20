import java.util.Collections;
import java.util.PriorityQueue;

//Max player
//Given piece, maximize score.
public class PieceNode {
	// <=9 since the sqaure block only has 9 possible moves
	static int CHILD_NO = 2;
	BoardNode parent;
	final int pieceIndex;
	BoardNode[] childBoards = new BoardNode[CHILD_NO];

	// slot and orientation
	BoardNode bestNode;

	static void setChildNum(int i) {
		CHILD_NO = i;
	}
	public PieceNode(BoardNode parent, int pieceIndex) {
		this.parent = parent;
		this.pieceIndex = pieceIndex;
		generateChildBoards();
	}
	
	//Iterate though possible next moves, keep top k
	public void generateChildBoards() {
		PriorityQueue<BoardNode> pq = new PriorityQueue<BoardNode>(64,Collections.reverseOrder());
		BitBoard bb;
		for ( int[] move : State.legalMoves[pieceIndex] ) {
			 bb = parent.BoardState.makeMove(move[0], move[1],pieceIndex);
			 pq.add(new BoardNode(this,bb,move));
		}
		//Keep top k BoardNodes
		for (int i=0;i<childBoards.length;i++) {
			childBoards[i] = pq.poll(); 
		}
	}

	//Sends command to increase depth by 1
	public void rootExpand() {
		for (BoardNode b : childBoards) {
			b.expand();
		}
	}
	
	//update ExpectiMiniMax values
	public void rootUpdate() {
		for (BoardNode b : childBoards) {
			b.score = b.update();
		}
	}
	
	//Assumes nodes are updated
	public int[] getBestMove() {
		bestNode = childBoards[0];
		for (BoardNode bn : childBoards) {
			if (bestNode.compareTo(bn) < 0) {
				bestNode = bn;
			}
		}
		return bestNode.move;
	}
	
	//Set root node 
	public PieceNode setRootToBest(int nextPiece) {
		this.parent = null;
		return bestNode.childPieces[nextPiece];
	}

	//returns updated ExpectiMiniMax value.
	//Called by parent BoardNode
	public float update() {
		float max = childBoards[0].update();
		float temp;
		for (BoardNode bn : childBoards) {
			temp = bn.update();
			if (max < temp) max = temp;
		}
		return max;
	}

	// TODO: Expand and update in one tree traversal.
	// Might be faster
	public void expandAndUpdate() {
		
	}
}
