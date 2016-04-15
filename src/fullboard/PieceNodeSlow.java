package fullboard;
import java.util.PriorityQueue;

//Max player
//Given piece, maximize score.
public class PieceNodeSlow {
	static int CHILD_NO;
	BoardNodeSlow parent;
	final int pieceIndex;
	BoardNodeSlow[] childBoards = new BoardNodeSlow[CHILD_NO];
	boolean expanded;

	// slot and orientation
	public BoardNodeSlow bestNode;
	
	// <=9 since the sqaure block only has 9 possible moves
	public static void setChildNum(int i) {
		CHILD_NO = i;
	}
	
	//Constructor
	public PieceNodeSlow(BoardNodeSlow parent, int pieceIndex) {
		this.parent = parent;
		this.pieceIndex = pieceIndex;
	}
	
	//Iterate though possible next moves, keep top k
	public void generateChildBoards() {
		expanded = true;
		PriorityQueue<BoardNodeSlow> pq = new PriorityQueue<BoardNodeSlow>(32);
		int[][] moves = BitBoardFullArray.legalMoves[pieceIndex];

		for (int i = 0; i < CHILD_NO; i++) {
			pq.add(new BoardNodeSlow(this,moves[i]));
		}
		for (int i = CHILD_NO; i < moves.length; i++) {
			pq.add(new BoardNodeSlow(this,moves[i]));
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
			for (BoardNodeSlow b : childBoards) {
				for (PieceNodeSlow p : b.childPieces) {
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
			for (BoardNodeSlow b : childBoards) {
				for (PieceNodeSlow p : b.childPieces) {
					p.expand();
				}
			}
		}
	}

	//update ExpectiMiniMax values
	public void rootUpdate() {
		for (BoardNodeSlow b : childBoards) {
			b.score = b.update();
		}
	}

	//returns updated ExpectiMiniMax value.
	//Called by parent BoardNodeSlow
	public double update() {
		double max = childBoards[0].update();
		double temp;
		for (BoardNodeSlow bn : childBoards) {
			temp = bn.update();
			if (max < temp) max = temp;
		}
		return max;
	}

	// For multiprocess, b.update() can only run after
	// all of b's PieceNodeSlow p has expanded
	public void rootExpandAndUpdate() {
		if (!expanded) {
			generateChildBoards();
		} else {
			for (BoardNodeSlow b : childBoards) {
				for (PieceNodeSlow p : b.childPieces) {
					p.expand();
				}
				b.score = b.update();
			}
		}
	}

	//Assumes nodes are updated
	public int[] getBestMove() {
		bestNode = childBoards[0];
		for (BoardNodeSlow bn : childBoards) {
			if (bestNode.compareTo(bn) < 0) {
				bestNode = bn;
			}
		}
		return bestNode.move;
	}
	
	//Set root node 
	public PieceNodeSlow setRootToBest(int nextPiece) {
		this.parent = null;
		return bestNode.childPieces[nextPiece];
	}

}
