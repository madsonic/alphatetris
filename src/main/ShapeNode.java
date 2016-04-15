package main;

class ShapeNode {

  final BitBoardCol MODEL;
  final int SHAPE;

  MoveNode[] CHILD_MOVES;
  boolean hasExpanded = false;

  ShapeNode(int shape, BitBoardCol model) {
    MODEL = model;
    SHAPE = shape;

  }

  MoveNode searchBestMove(int maxDepth, int beamWidth) {

  }
}
