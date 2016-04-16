package main;

//Each column is represented as a 32bit integer.
public class BitBoardCol {

	final static int COLS = 10;
	final static int ROWS = 20;

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
	public static final int[] P_ORIENTS = State.getpOrients();
	public static final int[][] P_WIDTH = State.getpWidth();
	public static final int[][] P_HEIGHT = State.getpHeight();
	public static final int[][][] P_BOTTOM = State.getpBottom();
	public static final int[][][] P_TOP = State.getpTop();

	//PIECE_BITS[#pieces][#orient][#width][#height]
	public static final int PIECE_BITS[][][][] = new int[7][4][4][ROWS];

	//Initialize PIECE_BITS
	static {
		for (int p = 0; p< State.N_PIECES; p++) {
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

	static int getBit( int bitCol, int rowIndex) {
		return (bitCol >> rowIndex) & 1;
	}

	//all legal moves - first index is piece type - then a list of 2-length arrays
	public static final int[][][] LEGAL_MOVES = new int[State.N_PIECES][][];

	//initialize LEGAL_MOVES
	static {
		//for each piece type
		for(int i = 0; i < State.N_PIECES; i++) {
			//figure number of legal moves
			int n = 0;
			for(int j = 0; j < P_ORIENTS[i]; j++) {
				//number of locations in this orientation
				n += COLS+1-P_WIDTH[i][j];
			}
			//allocate space
			LEGAL_MOVES[i] = new int[n][2];
			//for each orientation
			n = 0;
			for(int j = 0; j < P_ORIENTS[i]; j++) {
				//for each slot
				for(int k = 0; k < COLS+1-P_WIDTH[i][j];k++) {
					LEGAL_MOVES[i][n][State.ORIENT] = j;
					LEGAL_MOVES[i][n][State.SLOT] = k;
					n++;
				}
			}
		}
	}
	
	//Board representation
	private int[] field;
	private int[] top;

	//heuristic weights
	private double[] weights;
	// weight array indices
	public static final int 
			INDEX_AGGREGATE_HEIGHT = 0,
			INDEX_COMPLETED_LINES = 1,
			INDEX_HOLES = 2,
			INDEX_COL_TRANS = 3,
			INDEX_ROW_TRANS = 4,
			INDEX_LANDING_HEIGHT = 5,
			INDEX_TOP_PARITY = 6,
			INDEX_TOP_VARIETY = 7,
			INDEX_MINIMAX_TOP = 8,
			INDEX_SIDE_BUMPINESS = 9,
			INDEX_WELLS = 10;
	//  int INDEX_BUMPINESS = 11;

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

	//Constructor
	public BitBoardCol(int[] field, int[] top, double[] weights) {
		this.field = field.clone();
		this.top = top.clone();
		setWeights(weights);
	}

	public BitBoardCol(double[] weights) {
		this.field = new int[COLS];
		this.top = new int[COLS];
		setWeights(weights);
	}

	//Constructor
	public BitBoardCol(BitBoardCol bb) {
		this.field = bb.field.clone();
		this.top = bb.top.clone();
		this.weights = bb.weights;
	}

	public double makeMove(int orient, int slot, int nextPiece) {
		//height if the first column makes contact
		int height = top[slot]- P_BOTTOM[nextPiece][orient][0];

		//for each column beyond the first in the piece
		for(int c = 1; c < P_WIDTH[nextPiece][orient]; c++) {
			height = Math.max(height,top[slot+c]- P_BOTTOM[nextPiece][orient][c]);
		}
		landing_height = height + P_HEIGHT[nextPiece][orient]/2;
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
			bit1 = getBit(field[c],0);
            for (int r=1; r<top[c]; r++){
                bit2 = getBit(field[c],r);
                col_transitions += (bit1 ^ bit2);
                bit1 = bit2;
            }
		}

		for (int c= 1; c<COLS-1; c++) {
			bit1 = (field[c-1] & (~field[c]) & field[c+1]) >> top[c];
			bit1=Integer.bitCount(bit1);
			well += ((1+bit1)*bit1)/2; //AP sum
		}
		//well values for the side columns
		bit1 = ((~field[0]) & field[1]) >> top[0];
		bit2 = ((~field[COLS-1]) & field[COLS-2]) >> top[COLS-1];

		bit1 = Integer.bitCount(bit1);
		bit2 = Integer.bitCount(bit2);

		well += ( (1+bit1)*bit1 + (1+bit2)*bit2 )/2;

		//calculate row_transitions
		for (int r=0; r<top_max; r++){
			bit1 = 1;
            for (int c = 0; c<COLS; c++){
                bit2 = getBit(field[c],r);
                row_transitions += (bit1 ^ bit2);
                bit1 = bit2;
            }
            //for the last column
            row_transitions += (bit1 ^ 1);
		}

		for (int i = 0; i < 10; i+=2) {
			top_parity += (top[i] & 1) - (top[i+1] & 1);
		}

		return weights[INDEX_AGGREGATE_HEIGHT] * aggregate_height
				+ weights[INDEX_COMPLETED_LINES] * complete_lines
				+ weights[INDEX_HOLES] * holes
				+ weights[INDEX_COL_TRANS] * col_transitions
				+ weights[INDEX_ROW_TRANS] * row_transitions
				+ weights[INDEX_LANDING_HEIGHT] * landing_height
				+ weights[INDEX_TOP_PARITY] * top_parity
				+ weights[INDEX_TOP_VARIETY] * top_variety
				+ weights[INDEX_MINIMAX_TOP] * mini_max_top
				+ weights[INDEX_SIDE_BUMPINESS] * side_bump
				+ weights[INDEX_WELLS] * well;
				//+ weights[INDEX_BUMPINESS] * bumpiness;
	}

	public double getReward() {
		return ROWS*COLS - aggregate_height;
	}

	public void setWeights(double[] weights) { this.weights = weights; }

	public int[] getFieldCopy() { return field.clone(); }
	public int[] getTopCopy() { return top.clone(); }
	public double[] getWeights() { return weights; }
}
