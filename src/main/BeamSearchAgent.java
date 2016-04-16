package main;

public interface BeamSearchAgent {

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

    final int BEAM_WIDTH = 3;
    final int LOOKAHEAD_DEPTH = 1;
    final boolean MULTITHREAD = false;

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
