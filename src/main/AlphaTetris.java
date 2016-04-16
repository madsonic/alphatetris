package main;

import java.util.concurrent.*;

/**
 * Controls the search tree (piecenodes and boardnodes)
 * Implements search logic.
 */
public class AlphaTetris implements BeamSearchAgent {

  private double[] weights;
  private int maxDepth;
  private int beamWidth;

  private final ExecutorService EXECUTOR;

  private MoveNode root;

  public AlphaTetris(int beamWidth, int maxDepth, double[] weights, boolean concurrent) {
    if (beamWidth <= 0 || maxDepth < 0 || weights == null) throw new IllegalArgumentException();
    this.beamWidth = beamWidth;
    this.weights = weights;
    this.maxDepth = maxDepth;
    this.root = MoveNode.makeFirstNode(weights);

    if (concurrent) {
      int pcs = Runtime.getRuntime().availableProcessors();
      EXECUTOR = new ThreadPoolExecutor(
          pcs, pcs,
          10, TimeUnit.SECONDS,
          new ArrayBlockingQueue<Runnable>(pcs * 24, false),
          new ThreadPoolExecutor.CallerRunsPolicy()
      );
      ((ThreadPoolExecutor)EXECUTOR).allowCoreThreadTimeOut(true);
    } else {
      EXECUTOR = null;
    }
  }

  /////////////////////////////////////////////
  // boring setters getters
  /////////////////////////////////////////////

  public void setBeamWidth(int beamWidth) { this.beamWidth = beamWidth; }
  public int getBeamWidth() { return beamWidth; }

  @Override
  public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
  @Override
  public int getMaxDepth() { return maxDepth; }

  @Override
  public void setWeights(double[] weights) { this.weights = weights.clone(); }
  @Override
  public double[] getWeights() { return weights.clone(); }

  /////////////////////////////////////////////
  // search logic
  /////////////////////////////////////////////

  @Override
  public int[] pickNextMove(int givenShape) throws ExecutionException, InterruptedException {
    ShapeNode currentShape = root.getNextShapeNode(givenShape);
    root = currentShape.getBestMove(maxDepth, beamWidth, EXECUTOR);
    return root.MOVE;
  }

}
