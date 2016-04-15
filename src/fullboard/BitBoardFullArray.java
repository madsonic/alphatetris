package fullboard;

import main.State;

//Adapted from the state.java
//Each block is represented as a integer on int[][] field
public class BitBoardFullArray  {
	final static int COLS = 10;
	final static int ROWS = 20;

	//Board representation
	private int[][] field;
	private int[] top;

	static int[][][] legalMoves = new int[7][][];

	//heuristic parameters
	int aggregate_height;
	int complete_lines;
	int holes;
	int col_transitions;
	int row_transitions;
	int landing_height;
	int top_parity;
	int top_variety;
	int mini_max_top;
	int side_bump;
	int bumpiness;
	int well;

	//States for the 7 peices
	//States for the 7 pieces
	final static int[] P_ORIENTS = State.getpOrients();
	final static int[][] P_WIDTH = State.getpWidth();
	final static int[][] P_HEIGHT = State.getpHeight();
	final static int[][][] P_BOTTOM = State.getpBottom();
	final static int[][][] P_TOP = State.getpTop();

	
	//initialize legalMoves
	static {
		//for each piece type
		for(int i = 0; i < 7; i++) {
			//figure number of legal moves
			int n = 0;
			for(int j = 0; j < P_ORIENTS[i]; j++) {
				//number of locations in this orientation
				n += COLS+1-P_WIDTH[i][j];
			}
			//allocate space
			legalMoves[i] = new int[n][2];
			//for each orientation
			n = 0;
			for(int j = 0; j < P_ORIENTS[i]; j++) {
				//for each slot
				for(int k = 0; k < COLS+1-P_WIDTH[i][j];k++) {
					legalMoves[i][n][0] = j;
					legalMoves[i][n][1] = k;
					n++;
				}
			}
		}
	}

	//Constructor
	public BitBoardFullArray() {
		this.field = new int[20][10];
		this.top = new int[10];
	}
	
	public BitBoardFullArray(BitBoardFullArray bb) {
		this.field = new int[ROWS][COLS];
		for (int r=0; r<ROWS;r++) {
			this.field[r] = bb.field[r].clone();
		}
		this.top = bb.top.clone();
	}
	
	public double makeMove(int orient, int slot, int nextPiece) {
		//height if the first column makes contact
		int height = top[slot]-P_BOTTOM[nextPiece][orient][0];

		//for each column beyond the first in the piece
		for(int c = 1; c < P_WIDTH[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-P_BOTTOM[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+P_HEIGHT[nextPiece][orient] >= ROWS) {
			for (int c=0;c<COLS;c++){
				top[c]=ROWS;
			}
			return Integer.MIN_VALUE;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < P_WIDTH[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+P_BOTTOM[nextPiece][orient][i]; h < height+P_TOP[nextPiece][orient][i]; h++) {
				field[h][i+slot] = 1;
			}
		}

		//adjust top
		for(int c = 0; c < P_WIDTH[nextPiece][orient]; c++) {
			top[slot+c]=height+P_TOP[nextPiece][orient][c];
		}

		complete_lines = 0;
		//check for full rows - starting at the top
		for(int r = height+P_HEIGHT[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				complete_lines++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		return calcHeuristic();
	}

	public double calcHeuristic() {
		bumpiness = 0;
		aggregate_height = 0;
		holes = 0;

		for (int c=0;c<COLS-1;c++){
			bumpiness += Math.abs(top[c]-top[c+1]);
		}
		for (int c=0; c<COLS; c++){
			aggregate_height+=top[c];
			for (int r=0; r<top[c];r++) {
				if (field[r][c]==0) {
					holes++;
				}
			}
		}
		
		return -510*aggregate_height +761*complete_lines -357*holes -184*bumpiness;
	}

	public double getReward() {
		return ROWS*COLS - aggregate_height;
	}

}
