//Each column is represented as a 32bit integer.
public class BitBoardCol implements BitBoard {
	final static int COLS = 10;
	static int ROWS = 20;

	//pieceBits[#pieces][#orient][#width][#height]
	static int pieceBits[][][][] = new int[7][4][4][ROWS];
	int[] top;
	int[] field;
	float score;
	
	//heuristic variables
	//simple off-the-shelf heuristic
	int complete_lines;
	int aggregate_height;
	int bumpiness;
	int holes;

	//States for the 7 peices
	static int[] pOrients = {1,2,4,4,4,2,2};

	static int[][] pWidth = {
			{2},
			{1,4},
			{2,3,2,3},
			{2,3,2,3},
			{2,3,2,3},
			{3,2},
			{3,2}
	};
	static int[][] pHeight = {
			{2},
			{4,1},
			{3,2,3,2},
			{3,2,3,2},
			{3,2,3,2},
			{2,3},
			{2,3}
	};
	static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};

	static void setRows(int rowNum) {
		ROWS = rowNum;
	}
	static void initPieceBits() {
		for (int p=0; p<7; p++) {
			for (int o=0; o<pOrients[p]; o++) {
				for (int c=0; c<pWidth[p][o]; c++) {
					for(int h=pBottom[p][o][c]; h<pTop[p][o][c]; h++) {
						pieceBits[p][o][c][0] |= 1 << h;
					}
					for (int r=0; r<ROWS; r++) {
						pieceBits[p][o][c][r] = pieceBits[p][o][c][0] << r;
					}
				}
			}
		}
	}

	static int getBit( int bitCol, int rowIndex) {
		return (bitCol >> rowIndex) & 1;
	}

	public BitBoardCol( int[] field, int[] top) {
		this.field = field.clone();
		this.top = top.clone();
	}

	public boolean makeMoveP(int orient, int slot, int nextPiece) {
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];

		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] > ROWS) {
			for (int c=0;c<COLS;c++){
				top[c]=ROWS+1;
			}
			score = -11111111111f;
			return false;
		}

		//for each column in the piece - fill in the appropriate blocks
		//Alt: Use bit shift operation instead of storing pieceBits at different height.
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			field[c+slot] |= pieceBits[nextPiece][orient][c][height];
		}

		int rowFullBits = field[0];
		for (int r=1; r<COLS;r++) {
			rowFullBits &= field[r];
		}
		complete_lines = Integer.bitCount(rowFullBits);

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//if the row was full - remove it and slide above stuff down
			if(getBit(rowFullBits,r)==1) {
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
		score = calcHeuristic();
		return true;
	}
	
	@Override
	public BitBoardCol makeMove(int orient, int slot, int nextPiece) {
		BitBoardCol bb = new BitBoardCol(field, top);
		bb.makeMoveP(orient, slot, nextPiece);
		return bb;
	}

	@Override
	public float calcHeuristic() {
		bumpiness = 0;
		aggregate_height = 0;
		holes = 0;
		for (int c=0;c<COLS-1;c++){
			bumpiness += Math.abs(top[c]-top[c+1]);
		}
		for (int c=0; c<COLS; c++){
			aggregate_height+=top[c];
			holes += top[c] - Integer.bitCount(field[c]);
		}
		
		return -510*aggregate_height +761*complete_lines -357*holes -184*bumpiness;
	}

	@Override
	public float getScore() {
		return score;
	}

	@Override
	public float getValue() {
		return ROWS*COLS - aggregate_height;
	}

}
