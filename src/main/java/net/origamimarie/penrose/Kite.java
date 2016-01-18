package net.origamimarie.penrose;

/*
Here's a kite.  The vertices will be referred to by these letters.  Mirroring/rotation matters.
     m
  l_-'-_n
   \   /
    \ /
     V
     k
 */
public class Kite extends Shape {

  private static Point[][][] possibleVertexLocations;

  static {
    Point[] kBasedVertices = new Point[4];
    kBasedVertices[V.K.shapeBasedNumber] = new Point(0.0, 0.0);
    kBasedVertices[V.N.shapeBasedNumber] = new Point(1.0, 0.0);
    kBasedVertices[V.L.shapeBasedNumber] = new Point(Math.cos(Math.PI*0.4), Math.sin(Math.PI*0.4));
    kBasedVertices[V.M.shapeBasedNumber] = new Point(Math.cos(Math.PI*0.2), Math.sin(Math.PI*0.2));
    possibleVertexLocations = Shape.convertZeroBasedVerticesToAllStarterVertices(kBasedVertices);
  }

  public Kite() {
    shapeTypeName = "Kite";
    vNumberMappings = new V[]{V.K, V.L, V.M, V.N};
  }

  protected Point[][][] getPossibleVertexLocations() {
    return possibleVertexLocations;
  }

}
