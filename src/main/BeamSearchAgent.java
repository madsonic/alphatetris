package main;

import ga.PartialPlayerTwo;

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

  static void main(String[] args) {
    // best weights so far
    double[] weights = {
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
    BeamSearchAgent agent = makeDefaultAgent(3, 1, weights);
    State s = new State();
//    new TFrame(s);
    while (!s.hasLost()) {
      if (s.getTurnNumber() % 10000 == 0) {
        PartialPlayerTwo.printScore(s);
      }
      s.makeMove(agent.pickNextMove(s.getNextPiece()));
//      s.draw();
//      s.drawNext(4, 0);
    }
    System.out.println("You have completed "+s.getRowsCleared()+" rows.");
  }
}
