package net.origamimarie.penrose;


import java.util.ArrayList;
import java.util.List;

public abstract class Shape {

  private Vertex[] vertices;
  private Orientation orientation;

  public Shape() {
    vertices = new Vertex[4];
    for (int i = 0; i < 4; i++) {
      vertices[i] = new Vertex();
    }
    orientation = null;
  }

  public static Shape makeNew(Class<? extends Shape> clazz) {
    if(clazz == Dart.class) {
      return new Dart();
    } else if(clazz == Kite.class) {
      return new Kite();
    } else {
      return null;
    }
  }

  protected abstract Orientation getOrientation(V v, int clockwiseMostWedge);

  protected abstract V[] getVs();

  public void setOrientation(V v, int clockwiseMostWedge) {
    setOrientation(getOrientation(v, clockwiseMostWedge));
  }

  public Vertex getVertex(V v) {
    return vertices[v.shapeBasedNumber];
  }

  public void setVertex(Vertex vertex, V v) {
    vertices[v.shapeBasedNumber] = vertex;
    // We know what direction we're pointing and how we're attached to a vertex,
    // and that vertex knows where it is in space.
    // That means we can calculate locations for any other unlocated vertices.
    if (orientation != null && vertex.getLocation() != null) {
      Point knownPointBaseline = orientation.relativePoints[v.shapeBasedNumber];
      for (int i = 0; i < 4; i++) {
        if (i != v.shapeBasedNumber && vertices[i] != null) {
          vertices[i].setLocation(vertex.getLocation().plus(orientation.relativePoints[i]).minus(knownPointBaseline));
        }
      }
    }
  }

  public void setOrientation(Orientation orientation) {
    if (this.orientation == null) {
      this.orientation = orientation;
      for (V v : getVs()) {
        vertices[v.shapeBasedNumber].putShapeInWedges(this, v);
      }
    }
  }

  public Orientation getOrientation() {
    return orientation;
  }

  public List<Point[]> getAllShapePoints(List<Shape> visitedShapes) {
    List<Point[]> points = new ArrayList<>();
    Point[] myPoints = new Point[4];
    points.add(myPoints);
    for(int i = 0; i < 4; i++) {
      myPoints[i] = vertices[i].getLocation();
    }
    for(Vertex vertex : vertices) {
      if(vertex != null) {
        points.addAll(vertex.getAllShapePoints(visitedShapes));
      }
    }
    return points;
  }

}
