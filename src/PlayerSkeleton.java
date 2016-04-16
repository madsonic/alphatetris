import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveTask;

public class PlayerSkeleton {

	final double[] WEIGHTS = {
			-1.4101417483877365,
			1.7211254382260732,
			-9.430143863717376,
			-9.994060706564685,
			-2.151838313833677,
			-0.44094887506903957,
			-0.1085005610847889,
			2.575604041318804,
			-0.782307878163081,
			-0.2962660424028918,
			-2.8036583031140183
	};

	final int BEAM_WIDTH = 3;
	final int LOOKAHEAD_DEPTH = 1;
	final boolean MULTITHREAD = true;
	final BeamSearchAgent agent = BeamSearchAgent.makeDefaultAgent(
			BEAM_WIDTH, LOOKAHEAD_DEPTH, WEIGHTS, MULTITHREAD
	);

	//implement this function to have a working system
	public int[] pickMove(State s, int[][] legalMoves) {
		return agent.pickNextMove(s.getNextPiece());
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}




interface BeamSearchAgent {

	static BeamSearchAgent makeDefaultAgent(int beamWidth, int maxDepth, double[] weights, boolean concurrent) {
		return new AlphaTetris(beamWidth, maxDepth, weights, concurrent);
	}

	// sets beam search width (CHILDNUM)
	void setBeamWidth(int width);
	int getBeamWidth();
	// sets max search depth (0 = no look ahead, only consider current shape)
	void setMaxDepth(int maxDepth);
	int getMaxDepth();
	// sets heuristic weights
	void setWeights(double[] weights);
	double[] getWeights();
	//
	void setParallel(boolean parallel);

	// does not sync with State; agent follows its internal model.
	int[] pickNextMove(int givenShape);

	static void main(String[] args) {
		// best weights so far
		final double[] WEIGHTS = {
				-1.4101417483877365,
				1.7211254382260732,
				-9.430143863717376,
				-9.994060706564685,
				-2.151838313833677,
				-0.44094887506903957,
				-0.1085005610847889,
				2.575604041318804,
				-0.782307878163081,
				-0.2962660424028918,
				-2.8036583031140183
		};

		int BEAM_WIDTH = 2;
		int LOOKAHEAD_DEPTH = 1;
		boolean MULTITHREAD = true;

		if (args.length > 0) {
			BEAM_WIDTH = Integer.parseInt(args[0]);
			LOOKAHEAD_DEPTH = Integer.parseInt(args[1]);
			MULTITHREAD = args[2].equals("multi");
		}

		BeamSearchAgent agent = makeDefaultAgent(BEAM_WIDTH, LOOKAHEAD_DEPTH, WEIGHTS, MULTITHREAD);
		State s = new State();
//    new TFrame(s);
		long t = System.nanoTime();
		while (!s.hasLost()) {
			if (s.getTurnNumber() % 10000 == 0) {
				System.out.println(
						"Turn num: "+ s.getTurnNumber()
								+ "   Lines cleared: " + s.getRowsCleared()
								+ "   Turns per second: " + turnsPerSecond(10000, System.nanoTime()-t)
				);
				t = System.nanoTime();
			}
			s.makeMove(agent.pickNextMove(s.getNextPiece()));
//      s.draw();
//      s.drawNext(4, 0);
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

	static String turnsPerSecond(int turns, long nanos) {
		return "" + ((double)turns)*1000000000 / nanos;
	}
}


/**
 * Controls the search tree (piecenodes and boardnodes)
 * Implements search logic.
 */
class AlphaTetris implements BeamSearchAgent {

	private double[] weights;
	private int maxDepth;
	private int beamWidth;
	private boolean parallel;
	private int turnCount = 0;

	private MoveNode root;

	public AlphaTetris(int beamWidth, int maxDepth, double[] weights, boolean parallel) {
		if (beamWidth <= 0 || maxDepth < 0 || weights == null) throw new IllegalArgumentException();
		this.beamWidth = beamWidth;
		this.weights = weights;
		this.maxDepth = maxDepth;
		this.root = MoveNode.makeFirstNode(weights);
		this.parallel = parallel;
	}

	/////////////////////////////////////////////
	// boring setters getters
	/////////////////////////////////////////////

	public void setBeamWidth(int beamWidth) { this.beamWidth = beamWidth; }
	public int getBeamWidth() { return beamWidth; }

	@Override
	public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
	@Override
	public int getMaxDepth() { return maxDepth; }

	@Override
	public void setWeights(double[] weights) { this.weights = weights.clone(); }
	@Override
	public double[] getWeights() { return weights.clone(); }

	@Override
	public void setParallel(boolean p) { this.parallel = p; }

	/////////////////////////////////////////////
	// search logic
	/////////////////////////////////////////////

	@Override
	public int[] pickNextMove(int givenShape)  {
		turnCount++;
		ShapeNode currentShape = root.getNextShapeNode(givenShape);
		if (parallel) {
			root = currentShape.makeBestMoveTask(maxDepth, beamWidth).performTask();
		} else {
			root = currentShape.getBestMoveSequential(maxDepth, beamWidth);
		}
//    if (turnCount % 10000 == 0) { System.gc(); }
		return root.MOVE;
	}

}


//Each column is represented as a 32bit integer.
class BitBoardCol {

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


class ShapeNode {

	final BitBoardCol MODEL;
	final int SHAPE;
	final ShapeNode SELF = this;

	MoveNode[] moveChildren = {};

	ShapeNode(int shape, BitBoardCol model) {
		MODEL = model;
		SHAPE = shape;
	}

	MoveNode getBestMoveSequential(int remainingDepth, int beamWidth) {
		if (moveChildren.length != beamWidth) {
			moveChildren = generateChildren(beamWidth, SELF);
		}
		for (MoveNode child : moveChildren) {
			child.updateExpectedValueSequential(remainingDepth, beamWidth);
		}
		MoveNode best = moveChildren[0];
		for (MoveNode child : moveChildren) {
			if (child.compareTo(best) > 0) { best = child; }
		}
		return best;
	}

	GetBestMoveTask makeBestMoveTask(int remainingDepth, int beamWidth) {
		return new GetBestMoveTask(remainingDepth, beamWidth);
	}

	class GetBestMoveTask extends RecursiveTask<MoveNode> {
		final int REMAINING_DEPTH;
		final int BEAM_WIDTH;
		GetBestMoveTask(int remainingDepth, int beamWidth) {
			REMAINING_DEPTH = remainingDepth;
			BEAM_WIDTH = beamWidth;
		}

		@Override
		protected MoveNode compute() {
			if (REMAINING_DEPTH == 0) {
				return getBestMoveSequential(REMAINING_DEPTH, BEAM_WIDTH);
			}

			if (moveChildren.length != BEAM_WIDTH) {
				moveChildren = generateChildren(BEAM_WIDTH, SELF);
			}

			final List<ForkJoinTask<MoveNode>> tasks = new ArrayList<>(moveChildren.length-1);
			for (int i=0; i<moveChildren.length-1; i++) {
				tasks.add(
						moveChildren[i].makeUpdateExpectedValueTask(REMAINING_DEPTH, BEAM_WIDTH)
								.fork()
				);
			}

			MoveNode best = moveChildren[moveChildren.length-1]
					.makeUpdateExpectedValueTask(REMAINING_DEPTH, BEAM_WIDTH)
					.performTask();
			for (ForkJoinTask<MoveNode> task : tasks) {
				final MoveNode currentMove = task.join();
				if (currentMove.compareTo(best) > 0) { best = currentMove; }
			}
			return best;
		}

		MoveNode performTask() { return compute(); }

	}

	// calculates immediate heuristic values of all possible next moves,
	// prunes to beam width size
	public static MoveNode[] generateChildren(int beamWidth, ShapeNode parent) {
		final int[][] legalMoves = BitBoardCol.LEGAL_MOVES[parent.SHAPE];
		final PriorityBlockingQueue<MoveNode> pq = new PriorityBlockingQueue<>(40);

		for (int i = 0; i < legalMoves.length; i++) {
			pq.add(new MoveNode(legalMoves[i], parent));
			if (pq.size() > beamWidth) {
				pq.poll();
			}
		}
		return pq.toArray(new MoveNode[0]);
	}
}


class MoveNode implements Comparable<MoveNode> {

	final BitBoardCol MODEL;
	final int[] MOVE;
	final ShapeNode[] SHAPE_CHILDREN = new ShapeNode[State.N_PIECES];
	final MoveNode SELF = this;

	double expectedValue;

	static MoveNode makeFirstNode(double[] weights) {
		return new MoveNode(weights);
	}
	// used only through makeFirstNode
	private MoveNode(double[] weights) {
		MODEL = new BitBoardCol(weights);
		MOVE = null;
		// generate child pieces
		for (int i = 0; i< SHAPE_CHILDREN.length; i++) {
			SHAPE_CHILDREN[i] = new ShapeNode(i, this.MODEL); // shapes dont alter models, can pass reference to own MODEL
		}
	}

	MoveNode(int[] move, ShapeNode parent) {
		MOVE = move;
		MODEL = new BitBoardCol(parent.MODEL); // clone source MODEL
		expectedValue = MODEL.makeMove(move[State.ORIENT], move[State.SLOT], parent.SHAPE); // model updated

		// generate child pieces
		for (int i = 0; i< SHAPE_CHILDREN.length; i++) {
			SHAPE_CHILDREN[i] = new ShapeNode(i, this.MODEL); // shapes dont alter models, can pass reference to own MODEL
		}
	}

	// updates expected value and returns self
	MoveNode updateExpectedValueSequential(int remainingDepth, int beamWidth) {
		if (remainingDepth == 0) { return SELF; }

		expectedValue = 0;
		for (ShapeNode possibleShape : SHAPE_CHILDREN) {
			expectedValue += possibleShape.getBestMoveSequential(remainingDepth - 1, beamWidth).expectedValue;
		}
		expectedValue /= State.N_PIECES;
		return SELF;
	}

	UpdateExpectedValueTask makeUpdateExpectedValueTask(int remainingDepth, int beamWidth) {
		return new UpdateExpectedValueTask(remainingDepth, beamWidth);
	}

	class UpdateExpectedValueTask extends RecursiveTask<MoveNode> {
		final int REMAINING_DEPTH;
		final int BEAM_WIDTH;
		UpdateExpectedValueTask(int remainingDepth, int beamWidth) {
			REMAINING_DEPTH = remainingDepth;
			BEAM_WIDTH = beamWidth;
		}

		@Override
		protected MoveNode compute() {
			if (REMAINING_DEPTH == 0) {
				return SELF;
			}

			expectedValue = 0;
			final List<ForkJoinTask<MoveNode>> tasks = new ArrayList<>(SHAPE_CHILDREN.length);
			for (int i=0; i<SHAPE_CHILDREN.length-1; i++) {
				tasks.add(
						SHAPE_CHILDREN[i].makeBestMoveTask(REMAINING_DEPTH - 1, BEAM_WIDTH)
								.fork()
				);
			}
			expectedValue += SHAPE_CHILDREN[SHAPE_CHILDREN.length-1]
					.makeBestMoveTask(REMAINING_DEPTH - 1, BEAM_WIDTH)
					.performTask()
					.expectedValue / State.N_PIECES;
			for (ForkJoinTask<MoveNode> task : tasks) {
				expectedValue += task.join().expectedValue / State.N_PIECES;
			}
			return SELF;
		}

		MoveNode performTask() { return compute(); }
	}

	ShapeNode getNextShapeNode(int shape) { return SHAPE_CHILDREN[shape]; }

	@Override
	public int compareTo(MoveNode x) {
		return expectedValue < x.expectedValue ? -1
				: expectedValue > x.expectedValue ? 1
				: 0;
	}
}

