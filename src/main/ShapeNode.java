package main;

import java.util.Comparator;
import java.util.PriorityQueue;

class ShapeNode {

  final BitBoardCol MODEL;
  final int SHAPE;

  MoveNode[] moveChildren = {};

  ShapeNode(int shape, BitBoardCol model) {
    MODEL = model;
    SHAPE = shape;
  }

  MoveNode getBestMove(int remainingDepth, int beamWidth) {
    if (moveChildren.length != beamWidth) { // not yet expanded or beam width changed
      generateChildren(beamWidth);
    }
    for (MoveNode child : moveChildren) {
      child.updateExpectedValue(remainingDepth, beamWidth);
    }
    MoveNode best = moveChildren[0];
    for (MoveNode child : moveChildren) {
      if (child.compareTo(best) > 0) {
        best = child;
      }
    }
    return best;
  }

  // calculates immediate heuristic values of all possible next moves,
  // prunes to beam width size, saves to moveChildrem
  private void generateChildren(int beamWidth) {
    int[][] legalMoves = State.legalMoves[SHAPE];
    PriorityQueue<MoveNode> pq = new PriorityQueue<>(40);
    for (int i=0; i<legalMoves.length; i++) {
      pq.add(new MoveNode(legalMoves[i], this));
      if (pq.size() > beamWidth) {
        pq.poll();
      }
    }
    moveChildren = pq.toArray(new MoveNode[0]);
  }

}
