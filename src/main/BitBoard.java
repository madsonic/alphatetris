package main;
//BitBoard interface
//Heuristic function is located here since
//the calculation is closely tied to the board state.
public interface BitBoard {
	//Returns a new BitBoard given that the player makes a move.
	BitBoard makeMove(int orient, int slot, int nextPiece);

	double calcHeuristic();

	// Heuristic score at each state
	double getScore();
	
	public double getValue();

	void setWeights(double[] weights);
}