package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class ShapeNode {

  final BitBoardCol MODEL;
  final int SHAPE;

  MoveNode[] moveChildren = {};

  ShapeNode(int shape, BitBoardCol model) {
    MODEL = model;
    SHAPE = shape;
  }

  MoveNode getBestMove(int remainingDepth, int beamWidth, ExecutorService executor) throws ExecutionException, InterruptedException {

    if (moveChildren.length != beamWidth) { // not yet expanded or beam width changed
      generateChildren(beamWidth, executor);
    }
    MoveNode best;

    // single threaded version
    if (executor == null || remainingDepth == 0) { // dont split to threads at final level; not enough efficiency gain
      for (MoveNode child : moveChildren) {
        child.updateExpectedValue(remainingDepth, beamWidth, executor);
      }
      best = moveChildren[0];
      for (MoveNode child : moveChildren) {
        if (child.compareTo(best) > 0) { best = child; }
      }
    }

    // multithreaded version
    else {
      final List<Future<MoveNode>> futures = new ArrayList<>(moveChildren.length);
      for (MoveNode child : moveChildren) {
        futures.add(executor.submit(
            () ->
                child.updateExpectedValue(remainingDepth, beamWidth, executor)
        ));
      }
      best = futures.get(0).get();
      MoveNode current;
      for (Future<MoveNode> f : futures) {
        current = f.get();
        if (current.compareTo(best) > 0) { best = current; }
      }
    }

    return best;
  }

  // calculates immediate heuristic values of all possible next moves,
  // prunes to beam width size, saves to moveChildrem
  private void generateChildren(int beamWidth, ExecutorService executor) throws ExecutionException, InterruptedException {
    final int[][] legalMoves = State.legalMoves[SHAPE];
    final PriorityBlockingQueue<MoveNode> pq = new PriorityBlockingQueue<>(40);

    // single threaded version
//    if (executor == null) {
      for (int i = 0; i < legalMoves.length; i++) {
        pq.add(new MoveNode(legalMoves[i], this));
        if (pq.size() > beamWidth) {
          pq.poll();
        }
      }
//    }

    // multithreaded version
//    else {
//      final ShapeNode self = this;
//      int numTasks = legalMoves.length/10;
//      if (numTasks == 0) { numTasks = 1; }
//
//      final Future<?>[] futures = new Future[numTasks];
//      for (int i = 0; i < numTasks; i++) {
//        final int taskNum = i;
//        // each thread gets 10-20 MoveNode creations and insertions to the priority queue
//        futures[i] = executor.submit(()->{
//            for (int child = 10*taskNum; child < (10*taskNum) + 10 && child < legalMoves.length; child++) {
//              pq.add(new MoveNode(legalMoves[child], self));
//            }
//        });
//      }
//      for (Future<?> f : futures) { f.get(); } // wait for computations to finish
//      while (pq.size() > beamWidth) { pq.poll(); } // trim to beam width count
//    }

    moveChildren = pq.toArray(new MoveNode[0]);
  }
}
