package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RecursiveTask;

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
    final int[][] legalMoves = State.legalMoves[parent.SHAPE];
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


