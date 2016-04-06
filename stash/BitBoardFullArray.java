package main;
//Adapted from the state.java
//Each block is represented as a integer on int[][] field
public class BitBoardFullArray implements BitBoard {
	static final int ROWS = 20;
	static final int COLS = 10;
	int[][] field = new int[ROWS][COLS];
	int[] top;
	double score;
	static int[][][] moves;
	
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

	public BitBoardFullArray(int[][] field, int[] top) {
		this.field = new int[ROWS][COLS];
		for (int r=0; r<ROWS;r++) {
			this.field[r] = field[r].clone();
		}
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
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			for (int c=0;c<COLS;c++){
				top[c]=ROWS;
			}
			score = -11111111111f;
			return false;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = 1;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}

		complete_lines = 0;
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
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
		score = calcHeuristic();
		return true;
	}
	
	// TODO: refactoring this tgt with makeMoveP
	@Override
	public BitBoard makeMove(int orient, int slot, int nextPiece) {
		BitBoardFullArray bb = new BitBoardFullArray(field, top);
		bb.makeMoveP(orient, slot, nextPiece);
		return bb;
	}

	@Override
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

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public double getValue() {
		return COLS*ROWS - aggregate_height;
	}

}
