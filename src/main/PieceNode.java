package main;
import java.util.PriorityQueue;

//Max player
//Given piece, maximize score.
public class PieceNode {
	static int CHILD_NO;
	BoardNode parent;
	final int pieceIndex;
	BoardNode[] childBoards = new BoardNode[CHILD_NO];
	boolean expanded;

	// slot and orientation
	public BoardNode bestNode;
	
	// <=9 since the sqaure block only has 9 possible moves
	public static void setChildNum(int i) {
		CHILD_NO = i;
	}
	
	//Constructor
	public PieceNode(BoardNode parent, int pieceIndex) {
		this.parent = parent;
		this.pieceIndex = pieceIndex;
	}
	
	//Iterate though possible next moves, keep top k
	public void generateChildBoards() {
		expanded = true;
		PriorityQueue<BoardNode> pq = new PriorityQueue<BoardNode>(32);
		int[][] moves = State.legalMoves[pieceIndex];

		for (int i = 0; i < CHILD_NO; i++) {
			pq.add(new BoardNode(this,moves[i]));
		}
		for (int i = CHILD_NO; i < moves.length; i++) {
			pq.add(new BoardNode(this,moves[i]));
			pq.poll();
		}

		pq.toArray(childBoards);
	}

	//Sends command to increase depth by 1
	//Multi-process here
	public void rootExpand() {
		if (!expanded) {
			generateChildBoards();
		} else {
			for (BoardNode b : childBoards) {
				for (PieceNode p : b.childPieces) {
					p.expand();
				}
			}
		}
	}

	//Sends command to increase depth by 1
	public void expand() {
		if (!expanded) {
			generateChildBoards();
		} else {
			for (BoardNode b : childBoards) {
				for (PieceNode p : b.childPieces) {
					p.expand();
				}
			}
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
	public double update() {
		double max = childBoards[0].update();
		double temp;
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
