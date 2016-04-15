package main;

public interface BeamSearchAgent {

  static BeamSearchAgent makeDefaultAgent(int beamWidth, int maxDepth, double[] weights) {
    return new AlphaTetris(beamWidth, maxDepth, weights);
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

  // does not sync with State; agent follows its internal model.
  int[] pickNextMove(int givenShape);
}
