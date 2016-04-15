package main;

class MoveNode implements Comparable<MoveNode> {

  final BitBoardCol MODEL;
  final int[] MOVE;
  double expectedValue;
  final ShapeNode[] SHAPE_CHILDREN = new ShapeNode[State.N_PIECES];

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
  MoveNode updateExpectedValue(int remainingDepth, int beamWidth) {
    if (remainingDepth == 0) {
      return this;
    }

    //TODO parallel
    expectedValue = 0;
    remainingDepth--;
    for (ShapeNode possibleShape : SHAPE_CHILDREN) {
      expectedValue += possibleShape.getBestMove(remainingDepth, beamWidth).expectedValue;
    }
    expectedValue /= State.N_PIECES;
    return this;
  }

  ShapeNode getNextShapeNode(int shape) { return SHAPE_CHILDREN[shape]; }

  @Override
  public int compareTo(MoveNode x) {
    return expectedValue < x.expectedValue ? -1
        : expectedValue > x.expectedValue ? 1
        : 0;
  }

}
