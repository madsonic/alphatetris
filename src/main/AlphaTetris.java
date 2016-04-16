package main;

/**
 * Controls the search tree (piecenodes and boardnodes)
 * Implements search logic.
 */
public class AlphaTetris implements BeamSearchAgent {

  private double[] weights;
  private int maxDepth;
  private int beamWidth;
  private boolean parallel;
  private int turnCount = 0;

  private MoveNode root;

  public AlphaTetris(int beamWidth, int maxDepth, double[] weights, boolean parallel) {
    if (beamWidth <= 0 || maxDepth < 0 || weights == null) throw new IllegalArgumentException();
    this.beamWidth = beamWidth;
    this.weights = weights;
    this.maxDepth = maxDepth;
    this.root = MoveNode.makeFirstNode(weights);
    this.parallel = parallel;
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

  @Override
  public void setParallel(boolean p) { this.parallel = p; }

  /////////////////////////////////////////////
  // search logic
  /////////////////////////////////////////////

  @Override
  public int[] pickNextMove(int givenShape)  {
    turnCount++;
    ShapeNode currentShape = root.getNextShapeNode(givenShape);
    if (parallel) {
      root = currentShape.makeBestMoveTask(maxDepth, beamWidth).performTask();
    } else {
      root = currentShape.getBestMoveSequential(maxDepth, beamWidth);
    }
//    if (turnCount % 10000 == 0) { System.gc(); }
    return root.MOVE;
  }

}
