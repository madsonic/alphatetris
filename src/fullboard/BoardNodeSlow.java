package fullboard;

import main.State;

//Expected/min player
//Given board, get expected score.
public class BoardNodeSlow implements Comparable<BoardNodeSlow>{
	final PieceNodeSlow parent;
	public final BitBoardFullArray bb;
	//7 PieceNodes, 1 for each shape
	PieceNodeSlow[] childPieces = new PieceNodeSlow[7];

	final int[] move;
	double score;
	

	public BoardNodeSlow(PieceNodeSlow parent, int[] move) {
		this.parent = parent;
		this.move  = move;
		bb = new BitBoardFullArray(parent.parent.bb);
		score = bb.makeMove(move[0], move[1], parent.pieceIndex);
		generatePieceNodes();
	}

	// only used for first empty BB at start; all future bbs spawn from this one
	public BoardNodeSlow(BitBoardFullArray bb) {
		parent = null;
		move  = null;
		this.bb = bb;
	}
	
	public void generatePieceNodes() {
		for (int i=0; i<State.N_PIECES; i++) {
			childPieces[i] = new PieceNodeSlow(this,i);
		}
	}
	
	//updates child nodes and returns expected Score.
	//called by parent PieceNode
	public double update() {
		if (!childPieces[0].expanded) return score;
		float expected = 0;
		for (int i=0; i<State.N_PIECES;i++) {
			expected += childPieces[i].update();
		}
		return expected/7;
	}

	@Override
	public int compareTo(BoardNodeSlow o) {
		return this.score < o.score ? -1 : 1;
	}

}
