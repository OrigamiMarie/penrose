package net.origamimarie.penrose;

import java.util.ArrayList;
import java.util.List;

public class TilingGenerator {

  Vertex startingVertex;

  public TilingGenerator() {
    startingVertex = new Vertex();
    startingVertex.setLocation(new Point(0.0, 0.0));
    startingVertex.addShape(new Dart(), V.D, 0);
    startingVertex.addRandomShape();
  }

  public List<Point[]> getAllPointLists() {
    return startingVertex.getAllShapePoints(new ArrayList<Shape>());
  }

}
