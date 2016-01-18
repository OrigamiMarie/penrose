package net.origamimarie.penrose;


/*
Here's a dart.  The vertices will be referred to by these letters.  Mirroring/rotation matters.
  e_ f _g
   \-.-/
    \ /
     V
     d
 */
public class Dart extends Shape {

  private static Point[][][] possibleVertexLocations;

  static {
    Point[] dBasedVertices = new Point[4];
    dBasedVertices[V.D.shapeBasedNumber] = new Point(0.0, 0.0);
    dBasedVertices[V.G.shapeBasedNumber] = new Point(1.0, 0.0);
    dBasedVertices[V.L.shapeBasedNumber] = new Point(Math.cos(Math.PI*0.4), Math.sin(Math.PI*0.4));
    dBasedVertices[V.F.shapeBasedNumber] = new Point(0.5, Math.tan(Math.PI*0.2)/2.0);
    possibleVertexLocations = Shape.convertZeroBasedVerticesToAllStarterVertices(dBasedVertices);
  }

  public Dart() {
    shapeTypeName = "Dart";
    vNumberMappings = new V[]{V.D, V.E, V.F, V.G};
  }

  protected Point[][][] getPossibleVertexLocations() {
    return possibleVertexLocations;
  }

}
