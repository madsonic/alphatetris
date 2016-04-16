package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

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
