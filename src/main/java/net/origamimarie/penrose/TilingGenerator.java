package net.origamimarie.penrose;

import java.util.ArrayList;
import java.util.List;
import static net.origamimarie.penrose.Shape.V;

public class TilingGenerator {

  private List<Vertex> vertexList;

  public TilingGenerator() {
    vertexList = new ArrayList<>();
    Vertex startingVertex = new Vertex();
    vertexList.add(startingVertex);
    Shape shape = new Kite();
    startingVertex.putShapeCounterClockwise(shape, 1, V.D);
    Shape previousShape = shape;
    // First one was already placed, so we need four more for the total of five.
    for(int i = 0; i < 4; i++) {
      shape = new Kite();
      startingVertex.putShapeCounterclockwiseOf(shape, previousShape, V.D);
      previousShape = shape;
    }
    startingVertex.setLocation(new Point(0.0, 0.0));
    List<Point[]> pointLists = new ArrayList<>();
    startingVertex.getAllShapePoints(new ArrayList<Shape>(), pointLists);
  }

  public List<Point[]> getAllPointLists() {
    List<Shape> shapesProcessed = new ArrayList<>();
    List<Point[]> pointLists = new ArrayList<>();
    // This is probably completely redundant,
    // since probably all vertices are networked together by shapes.
    // It'll find a way to bite me if I don't do them all though.
    for(Vertex vertex : vertexList) {
      vertex.getAllShapePoints(shapesProcessed, pointLists);
    }
    return pointLists;
  }

}
