package main;
//Each column is represented as a 32bit integer.
public class BitBoardCol {

	final static int COLS = 10;
	static int ROWS = 20;

	//Board representation
	int[] field;
	int[] top;

	//heuristic weights
	static double weightAggregateHeight;
	static double weightCompleteLines;
	static double weightHoles;
	static double weightColTrans;
	static double weightRowTrans;
	static double weightLandingHeight;
    static double weightTopParity;
    static double weightTopVariety;
    static double weightMiniMaxTop;
    static double weightSideBump;
	static double weightBumpiness;

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
		int bit1, bit2;
		int top_max=0;
		int top_min=20;
		int[] varietyArr = new int[5];

		for (int i : top) {
			top_max = Math.max(i, top_max);
			top_min = Math.min(i, top_min);
		}

		for (int c=0;c<COLS-1;c++){
			bit1 = top[c]-top[c+1];
			bumpiness += Math.abs(bit1);
			if (bit1 == -2 || bit1 == 2) varietyArr[bit1+2] = 1;
			if (bit1 == -1 || bit1 == 1) varietyArr[bit1+2] = 2;
			if (bit1 == 0) varietyArr[bit1+2] = 2;
		}

		for ( int b: varietyArr ) top_variety += b;

		mini_max_top = top_max - top_min;
		side_bump = Math.abs(top[9]-top[8]) + Math.abs(top[0]-top[1]);
		
		for (int c=0; c<COLS; c++){
			aggregate_height+=top[c];
			holes += top[c] - Integer.bitCount(field[c]);
			bit1 = 1;
            for (int r=1; r<top[c]; r++){
                bit2 = getBit(field[c],r);
                col_transitions += (bit1 ^ bit2);
                bit1 = bit2;
            }
		}
		
		for (int r=0; r<top_max; r++){
			bit1 = getBit(field[0],r);
            for (int c = 1; c<COLS; c++){
                bit2 = getBit(field[c],r);
                row_transitions += (bit1 ^ bit2);
                bit1 = bit2;
            }
		}

		for (int i = 0; i < 10; i+=2) {
			top_parity += (top[i] & 1) - (top[i+1] & 1);
		}

		return  - weightAggregateHeight * aggregate_height
				+ weightCompleteLines * complete_lines
				- weightHoles * holes
				- weightColTrans * col_transitions
				- weightRowTrans * row_transitions
				- weightLandingHeight * landing_height
                - weightTopParity * top_parity
                + weightTopVariety * top_variety
                - weightMiniMaxTop * mini_max_top
                - weightSideBump * side_bump
                - weightBumpiness * bumpiness;
	}

	public double getReward() {
		return ROWS*COLS - aggregate_height;
	}

	public static void setWeights(double[] weights) {
		weightAggregateHeight = weights[0];
		weightCompleteLines = weights[1];
		weightHoles = weights[2];
		weightColTrans = weights[3];
		weightRowTrans = weights[4];
		weightLandingHeight = weights[5];
	    weightTopParity = weights[6];
	    weightTopVariety = weights[7];
	    weightMiniMaxTop = weights[8];
	    weightSideBump= weights[9];
	    //weightBumpiness = weights[10];
	}
}
