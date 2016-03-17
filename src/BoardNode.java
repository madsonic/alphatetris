//Expected/min player
//Given board, get expected score.
public class BoardNode implements Comparable<BoardNode>{
	final PieceNode parent;
	final BitBoard BoardState;
	//7 PieceNodes, 1 for each shape
	PieceNode[] childPieces = new PieceNode[7];

	final int[] move;
	float score;
	boolean expanded = false;

	public BoardNode(PieceNode parent, BitBoard state, int[] move) {
		this.parent = parent;
		this.BoardState = state;
		this.move = move;
		this.score = state.score;
	}
	
	public void generatePieceNodes() {
		for (int i=0; i<7; i++) {
			childPieces[i] = new PieceNode(this,i);
		}
	}
	
	//updates child nodes and returns expected Score.
	//called by parent PieceNode
	public float update(){
		if (!expanded) return score;
		float expected=0;
		for (int i=1; i<7;i++) {
			expected += childPieces[i].update();
		}
		return expected/7;
	}
	
	//if expanded, pass along the expand command to the next BoardNode
	//else, initiate PieceNodes
	public void expand() {
		
		if (expanded) {
			for(PieceNode p: childPieces) {
				for (BoardNode b : p.childBoards) {
					b.expand();
				}
			}
		}
		else {
			expanded = true;
			generatePieceNodes();
		}
	}

	@Override
	public int compareTo(BoardNode o) {
		return this.score < o.score ? -1 : 1;
	}

}
