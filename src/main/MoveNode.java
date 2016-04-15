package main;

class MoveNode implements Comparable<MoveNode> {

  final BitBoardCol MODEL;
  final int[] MOVE;
  final ShapeNode[] CHILD_SHAPES = new ShapeNode[State.N_PIECES];

  double value;

  static MoveNode makeFirstNode(double[] weights) {
    return new MoveNode(weights);
  }
  // used only through makeFirstNode
  private MoveNode(double[] weights) {
    MODEL = new BitBoardCol(weights);
    MOVE = null;

    // generate child pieces
    for (int i = 0; i< CHILD_SHAPES.length; i++) {
      CHILD_SHAPES[i] = new ShapeNode(i, this.MODEL); // shapes dont alter models, can pass reference to own MODEL
    }
  }

  MoveNode(int[] move, ShapeNode parent) {
    MOVE = move;
    MODEL = new BitBoardCol(parent.MODEL); // clone source MODEL
    value = MODEL.makeMove(move[State.ORIENT], move[State.SLOT], parent.SHAPE); // model updated

    // generate child pieces
    for (int i = 0; i< CHILD_SHAPES.length; i++) {
      CHILD_SHAPES[i] = new ShapeNode(i, this.MODEL); // shapes dont alter models, can pass reference to own MODEL
    }
  }

  int expand(int remainingDepth) {

  }

  ShapeNode getNextShapeNode(int shape) { return CHILD_SHAPES[shape]; }

  @Override
  public int compareTo(MoveNode x) { return value < x.value ? -1 : 1; }
}
