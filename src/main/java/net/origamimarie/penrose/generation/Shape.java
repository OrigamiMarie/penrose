package net.origamimarie.penrose.generation;


import java.util.ArrayList;
import java.util.Arrays;
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

  protected abstract Orientation getOrientation(Vwedge vwedge, int clockwiseMostWedge);

  protected abstract V[] getVs();

  public List<Vertex> getVertices() {
    List<Vertex> verticesList = new ArrayList<>();
    verticesList.addAll(Arrays.asList(vertices));
    return verticesList;
  }

  public void setOrientation(Vwedge vwedge, int wedgeLocation) {
    setOrientation(getOrientation(vwedge, wedgeLocation));
  }

  public Vertex getCounterclockwiseVertex(Vertex v) {
    for(int i = 0; i < vertices.length; i++) {
      if(vertices[i] == v) {
        return vertices[(i-1+vertices.length) % vertices.length];
      }
    }
    return null;
  }

  public Vertex getVertex(Vwedge vwedge) {
    return vertices[vwedge.v.shapeBasedNumber];
  }

  public void replaceVertex(Vertex original, Vertex replacement) {
    for(int i = 0; i < vertices.length; i++) {
      if(vertices[i] == original) {
        vertices[i] = replacement;
        setLocation(replacement, i);
        // The same vertex object better not be in more than one vertex slot.
        // Once we've found and replaced it, that should be it.
        return;
      }
    }
    // Bad news if we got down here.
    throw new IllegalArgumentException("This shape did not have the requested vertex to replace");
  }

  private void setLocation(Vertex referenceVertex, int referenceIndex) {
    // Thanks to the new vertex, we know where we are.
    // Notify all of the other vertices, if they need to know.
    if(orientation != null && referenceVertex.getLocation() != null) {
      Point knownBaseline = orientation.relativePoints[referenceIndex];
      for(int i = 0; i < 4; i++) {
        if(i != referenceIndex && vertices[i] != null && vertices[i].getLocation() == null) {
          vertices[i].setLocation(referenceVertex.getLocation().plus(orientation.relativePoints[i]).minus(knownBaseline),
                  referenceVertex);
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

  public Point[] getShapePoints() {
    Point[] myPoints = new Point[4];
    for(int i = 0; i < 4; i++) {
      myPoints[i] = vertices[i].getLocation();
    }
    return myPoints;
  }

}
