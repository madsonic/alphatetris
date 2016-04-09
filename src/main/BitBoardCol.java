package main;
//Each column is represented as a 32bit integer.
public class BitBoardCol {

	final static int COLS = 10;
	static int ROWS = 20;

	//Board representation
	int[] field;
	int[] top;

	//heuristic weights
	static double weightCompleteLines;
	static double weightAggregateHeight;
	static double weightBumpiness;
	static double weightHoles;
	static double weightColTrans;
	static double weightRowTrans;
	static double weightLandingHeight;

	//heuristic parameters
	int complete_lines;
	int aggregate_height;
	int bumpiness;
	int holes;
	int col_transitions;
	int row_transitions;
	int landing_height;

	/**
	 * The 7 piece shapes by index:
	 * 0: O
	 * 1: I
	 * 2: L
	 * 3: J
	 * 4: T
	 * 5: S
	 * 6: Z
	 */

	//States for the 7 pieces
	final static int[] P_ORIENTS = State.getpOrients();
	final static int[][] P_WIDTH = State.getpWidth();
	final static int[][] P_HEIGHT = State.getpHeight();
	final static int[][][] P_BOTTOM = State.getpBottom();
	final static int[][][] P_TOP = State.getpTop();

	//PIECE_BITS[#pieces][#orient][#width][#height]
	final static int PIECE_BITS[][][][] = new int[7][4][4][ROWS];

	//Initialize PIECE_BITS
	static {
		for (int p=0; p<State.N_PIECES; p++) {
			for (int o = 0; o< P_ORIENTS[p]; o++) {
				for (int c = 0; c< P_WIDTH[p][o]; c++) {
					for(int h = P_BOTTOM[p][o][c]; h< P_TOP[p][o][c]; h++) {
						PIECE_BITS[p][o][c][0] |= 1 << h; //
					}
					for (int r=0; r<ROWS; r++) {
						PIECE_BITS[p][o][c][r] = PIECE_BITS[p][o][c][0] << r;
					}
				}
			}
		}
	}

	static void setRows(int rowNum) {
		ROWS = rowNum;
	}

	static int getBit( int bitCol, int rowIndex) {
		return (bitCol >> rowIndex) & 1;
	}

	//Constructor
	public BitBoardCol( int[] field, int[] top) {
		this.field = field.clone();
		this.top = top.clone();
	}

	//Constructor
	public BitBoardCol(BitBoardCol bb) {
		this.field = bb.field.clone();
		this.top = bb.top.clone();
	}

	public double makeMove(int orient, int slot, int nextPiece) {
		//height if the first column makes contact
		int height = top[slot]- P_BOTTOM[nextPiece][orient][0];

		//for each column beyond the first in the piece
		for(int c = 1; c < P_WIDTH[nextPiece][orient]; c++) {
			height = Math.max(height,top[slot+c]- P_BOTTOM[nextPiece][orient][c]);
		}
		landing_height = height;
		//check if game ended
		if(height+ P_HEIGHT[nextPiece][orient] > ROWS) {
			for (int c=0;c<COLS;c++){
				top[c]=ROWS+1;
			}
			return Integer.MIN_VALUE;
		}

		//for each column in the piece - fill in the appropriate blocks
		//Alt: Use bit shift operation instead of storing PIECE_BITS at different height.
		for(int c = 0; c < P_WIDTH[nextPiece][orient]; c++) {
			field[c+slot] |= PIECE_BITS[nextPiece][orient][c][height];
		}

		int rowFullBits = field[0];
		for (int r=1; r<COLS;r++) {
			rowFullBits &= field[r];
		}

		//check for full rows - starting at the top
		for(int r = height+ P_HEIGHT[nextPiece][orient]-1; r >= height; r--) {
			//if the row was full - remove it and slide above stuff down
			if(getBit(rowFullBits,r)==1) {
				complete_lines++;
				//for each column
				for(int c = 0; c < COLS; c++) {
					//slide down all bricks
					field[c] = field[c] ^ ((field[c] ^ field[c]>>1) & ~((1<<r)-1));
				}
			}
		}
		//Update top;
		for (int c=0; c<COLS; c++) {
			top[c] = 32 - Integer.numberOfLeadingZeros(field[c]);
		}
		return calcHeuristic();
	}

	public double calcHeuristic() {
		int bit1;
		int bit2;
		for (int c=0;c<COLS-1;c++){
			bumpiness += Math.abs(top[c]-top[c+1]);
		}
		for (int c=0; c<COLS; c++){
			aggregate_height+=top[c];
			holes += top[c] - Integer.bitCount(field[c]);
			bit1 = 1;
			for (int r=top[c]-1; r>=0; r--){
				bit2 = getBit(c,r);
				col_transitions += (bit1 ^ bit2) & bit1;
			}
		}

		return  - weightAggregateHeight * aggregate_height
				+ weightCompleteLines * complete_lines
				- weightHoles * holes
				- weightBumpiness * bumpiness
				- weightColTrans * col_transitions
				- weightRowTrans * row_transitions
				- weightLandingHeight * landing_height;
	}

	public double getReward() {
		return ROWS*COLS - aggregate_height;
	}

	public static void setWeights(double[] weights) {
		weightAggregateHeight = weights[0];
		weightCompleteLines = weights[1];
		weightBumpiness = weights[2];
		weightHoles = weights[3];
		weightColTrans = weights[4];
		weightRowTrans = weights[5];
		weightLandingHeight = weights[6];
	}
}
