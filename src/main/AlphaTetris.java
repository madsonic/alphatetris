package main;

/**
 * Controls the search tree (piecenodes and boardnodes)
 * Implements search logic.
 */
public class AlphaTetris implements BeamSearchAgent {

  private double[] weights;
  private int maxDepth;
  private int beamWidth;

  private MoveNode root;

  public AlphaTetris(int beamWidth, int maxDepth, double[] weights) {
    this.beamWidth = beamWidth;
    this.weights = weights;
    this.maxDepth = maxDepth;
    this.root = MoveNode.makeFirstNode(weights);
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
  public int[] pickNextMove(int givenShape) {
    ShapeNode currentShape = root.getNextShapeNode(givenShape);
    root = currentShape.getBestMove(maxDepth);
    return root.MOVE;
  }

}
