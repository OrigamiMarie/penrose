package net.origamimarie.penrose;


import java.util.List;

public abstract class Shape {

  protected String shapeTypeName;
  protected V[] vNumberMappings;

  private Vertex[] vertices;
  private Shape[] neighbors;

  private boolean locationIsSet;
  private Point[] points;

  public Shape() {
    vertices = new Vertex[4];
    // Every shape comes with a full complement of vertices from the beginning.
    // They then get merged with the vertices of other shapes.
    for(int i = 0; i < 4; i++) {
      vertices[i] = new Vertex();
    }
    neighbors = new Shape[4];
    locationIsSet = false;
    points = new Point[4];
  }

  protected abstract Point[][][] getPossibleVertexLocations();

  public void setLocation(Vertex vertex, Point vertexLocation, int orientation) {
    // Only need to do this once.
    if(!locationIsSet) {
      locationIsSet = true;
      // It's the shape's job to figure out where all of its vertices should be.
      // Based on where the given vertex is located
      // and the orientation that the vertex thinks we're in,
      // we calculate each other vertex's location is and pass that information on.

      // So, first find out which one of our vertices this vertex is stuck to.
      V v = whichVisThisVertexOn(vertex);
      // Then get the base locations of all of the vertices based on
      // pre-calculated locations, our V type, and orientation.
      // Make sure this is a copy, we don't want to be messing with the original.
      System.arraycopy(getPossibleVertexLocations()[v.shapeBasedNumber][orientation], 0, points, 0, 4);
      // Then offset those vertices by the vertex's location,
      // so we end up at the correct point in space instead of at the origin.
      for(int i = 0; i < 4; i++) {
        points[i] = points[i].plus(vertexLocation);
      }

      // Now go through our vertices and update them.
      // They'll update & propagate, or ignore, based on whether their locations have been set or not.
      for(int i = 0; i < 4; i++) {
        if(vertices[i] != null) {
          vertices[i].setLocation(points[i]);
        }
      }
    }
  }

  public void getAllShapePoints(List<Shape> shapesAlreadyGotten, List<Point[]> pointLists) {
    if(!shapesAlreadyGotten.contains(this)) {
      shapesAlreadyGotten.add(this);
      pointLists.add(points);
      for(Shape shape : neighbors) {
        if(shape != null) {
          shape.getAllShapePoints(shapesAlreadyGotten, pointLists);
        }
      }
    }
  }

  public String getShapeTypeName() {
    return shapeTypeName;
  }

  public void setNeighborCounterClockwise(Shape neighbor, Vertex vertex) {
    setNeighbor(neighbor, vertex, false, true);
  }

  public void setNeighborClockwise(Shape neighbor, Vertex vertex) {
    setNeighbor(neighbor, vertex, true, true);
  }

  public void setVertex(Vertex vertex, V v) {
    vertices[v.shapeBasedNumber] = vertex;
  }

  private void setNeighbor(Shape neighbor, Vertex vertex, boolean clockwise, boolean propagate) {
    V v = whichVisThisVertexOn(vertex);
    // We want the E that is clockwise/counterclockwise from this V.
    // We're doing this opposite of the called one intentionally.
    E e = clockwise ? E.getCounterclockwiseFromV(v) : E.getClockwiseFromV(v);
    if(neighbors[e.shapeBasedNumber] != null) {
      throw new IllegalArgumentException(String.format("There is already a shape in neighbors[%s]",
              e.toString()));
    }
    Vertex otherVertexInCommon = clockwise ? getVertexCounterclockwiseFrom(v) : getVertexClockwiseFrom(v);
    if(otherVertexInCommon != null) {
      Vertex adoptedVertex = clockwise ?
              neighbor.getVertexCounterclockwiseFrom(vertex) :
              neighbor.getVertexClockwiseFrom(vertex);
      adoptedVertex.mergeVertexIntoSelf(otherVertexInCommon);
      vertices[v.shapeBasedNumber] = adoptedVertex;
    }
    neighbors[e.shapeBasedNumber] = neighbor;
    // Now set self on the neighbor.
    if(propagate) {
      neighbor.setNeighbor(this, vertex, !clockwise, false);
    }
  }

  protected Vertex getVertexClockwiseFrom(Vertex vertex) {
    return getVertexClockwiseFrom(whichVisThisVertexOn(vertex));
  }

  protected Vertex getVertexCounterclockwiseFrom(Vertex vertex) {
    return getVertexCounterclockwiseFrom(whichVisThisVertexOn(vertex));
  }

  protected Vertex getVertexClockwiseFrom(V v) {
    return vertices[v.clockwise().shapeBasedNumber];
  }

  protected Vertex getVertexCounterclockwiseFrom(V v) {
    return vertices[v.counterclockwise().shapeBasedNumber];
  }

  // Figure out where that vertex is on this shape.
  private V whichVisThisVertexOn(Vertex vertex) {
    V v = null;
    for(int i = 0; i < 4; i++) {
      if(vertices[i] == vertex) {
        v = vNumberMappings[i];
      }
    }
    if(v == null) {
      throw new IllegalArgumentException(String.format("This shape does not have a vertex %s",
              vertex));
    }
    return v;
  }

  protected static Point[][][] convertZeroBasedVerticesToAllStarterVertices(Point[] zeroVertices) {
    Point[][][] allVertices = new Point[4][10][4];
    allVertices[0][0] = zeroVertices;
    // Populate them by offsets of each other.
    // This makes a shape centered on each vertex.
    for(int i = 0; i < 4; i++) {
      for(int j = 0; j < 4; j++) {
        allVertices[i][0][j] = zeroVertices[i].minus(zeroVertices[j]);
      }
    }
    // Now we need to copy these all out to their many orientations.
    for(int i = 0; i < 4; i++) {
      for(int j = 0; j < 4; j++) {
        for(int k = 1; k < 10; k++) {
          double angle = Math.PI*0.2 * k;
          double cosAngle = Math.cos(angle);
          double sinAngle = Math.sin(angle);
          Point vertex = allVertices[i][0][j];
          allVertices[i][k][j] = new Point(
                  vertex.x * cosAngle - vertex.y * sinAngle,
                  vertex.x * sinAngle + vertex.y * cosAngle
          );
        }
      }
    }
    return allVertices;
  }

  // Refer to the ascii art at the tops of the Dart and Kite files for the locations.
  // The sequence starting with D is for Darts, K sequence is for Kites.
  public enum V {
    D(0, 0, 2),
    E(1, 1, 1),
    F(2, 2, 6),
    G(3, 3, 1),
    K(4, 0, 2),
    L(5, 1, 2),
    M(6, 2, 4),
    N(7, 3, 2);

    private static V[] clockwiseFromVnumber;
    private static V[] counterClockwiseFromVnumber;

    static {
      clockwiseFromVnumber = new V[]{V.E, V.F, V.G, V.D, V.L, V.M, V.N, V.K};
      counterClockwiseFromVnumber = new V[]{V.G, V.D, V.E, V.F, V.N, V.K, V.L, V.M};
    }

    public final int number;
    public final int shapeBasedNumber;
    public final int vertexSize;

    V(int number, int shapeBasedNumber, int vertexSize) {
      this.number = number;
      this.shapeBasedNumber = shapeBasedNumber;
      this.vertexSize = vertexSize;
    }

    public V clockwise() {
      return clockwiseFromVnumber[this.number];
    }

    public V counterclockwise() {
      return counterClockwiseFromVnumber[this.number];
    }
  }

  public enum E {
    DE(0, 0),
    EF(1, 1),
    FG(2, 2),
    GD(3, 3),
    KL(4, 0),
    LM(5, 1),
    MN(6, 2),
    NK(7, 3);

    private static E[] clockwiseFromVnumber;
    private static E[] counterclockwiseFromVnumber;

    static {
      clockwiseFromVnumber = new E[]{DE, EF, FG, GD, KL, LM, MN, NK};
      counterclockwiseFromVnumber = new E[]{GD, DE, EF, FG, NK, KL, LM, MN};
    }
    /*
    private static E[] counterClockwiseNeighbor;
    private static E[] clockwiseNeighbor;

    static {
      clockwiseNeighbor = new E[]{EF, FG, GD, DE, LM, MN, NK, KL};
      counterClockwiseNeighbor = new E[]{GD, DE, EF, FG, NK, KL, LM, MN};
    }*/

    public final int number;
    public final int shapeBasedNumber;

    E(int number, int shapeBasedNumber) {
      this.number = number;
      this.shapeBasedNumber = shapeBasedNumber;
    }

    public static E getClockwiseFromV(V v) {
      return clockwiseFromVnumber[v.number];
    }

    public static E getCounterclockwiseFromV(V v) {
      return counterclockwiseFromVnumber[v.number];
    }
/*
    public E getClockwiseNeighbor() {
      return clockwiseNeighbor[this.number];
    }

    public E getCounterclockwiseNeighbor() {
      return counterClockwiseNeighbor[this.number];
    }

    // These two are opposite because rotating around a vertex
    // is the opposite of rotating around the shape.
    public E getClockwiseAroundVertex() {
      return getCounterclockwiseNeighbor();
    }

    public E getCounterclockwiseAroundVertex() {
      return getClockwiseNeighbor();
    }
    */
  }

}
