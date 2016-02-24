package net.origamimarie.penrose.coloring;

import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.generation.Point;
import net.origamimarie.penrose.generation.Shape;
import net.origamimarie.penrose.generation.V;
import net.origamimarie.penrose.generation.Vertex;
import net.origamimarie.penrose.generation.Vwedge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ShapeGroup {

  private List<Shape> shapes;
  private GroupNeighborCircle neighborCircle;
  private ColoredShapeGroup coloredShapeGroup;
  private Point[] shapePoints;

  public ShapeGroup(List<Shape> shapes) {
    this.shapes = shapes;
    neighborCircle = new GroupNeighborCircle();
    calculateShapePoints();
  }

  private void calculateShapePoints() {
    // The shapes have been provided in a continuous counterclockwise fan.
    // They may loop around in a series of 5 shapes.
    // If the don't loop around, the center vertex needs to be generated too.
    // There's a duplicate point at the end.
    int pointCount = shapes.size() == 5 ? 11 : shapes.size()*2 + 3;
    shapePoints = new Point[pointCount];
    int pointNumber = 0;
    for(Shape shape : shapes) {
      // This is taking advantage of the fact that G0 and N0
      // resolve to the same vertex on Darts and Kites.
      shapePoints[pointNumber] = shape.getVertex(Vwedge.G0).getLocation();
      pointNumber++;
      // And of course, likewise with F0 and M0.
      shapePoints[pointNumber] = shape.getVertex(Vwedge.F0).getLocation();
      pointNumber++;
    }
    // Last point on last shape.
    shapePoints[pointNumber] = shapes.get(shapes.size()-1).getVertex(Vwedge.E0).getLocation();
    pointNumber++;
    // Hit the center vertex and starting vertex again if we need to.
    if(shapes.size() < 5) {
      shapePoints[pointNumber] = shapes.get(0).getVertex(Vwedge.D0).getLocation();
      pointNumber++;
      shapePoints[pointNumber] = shapes.get(0).getVertex(Vwedge.G0).getLocation();
    }
  }

  public Set<ShapeGroup> getNeighboringIsland() {
    return neighborCircle.getNeighboringIsland();
  }

  public void setColoredShapeGroup(ColoredShapeGroup coloredShapeGroup) {
    this.coloredShapeGroup = coloredShapeGroup;
  }

  public ColoredShapeGroup getColoredShapeGroup() {
    return coloredShapeGroup;
  }

  public Set<ShapeGroup> getNeighbors() {
    return neighborCircle.getNeighborsSetCopy();
  }

  public List<ShapeGroup> getNeighborsList() {
    return neighborCircle.getNeighborsListCopy();
  }

  public List<Point[]> getShapePoints() {
    return Collections.singletonList(shapePoints);
  }

  // Keep only the ShapeGroups that have at least one point inside the rectangle.
  public static Collection<ShapeGroup> generateShapeGroups(Collection<Vertex> vertices, ShapeGroupType groupType, Point low, Point high) {
    Map<Shape, ShapeGroup> shapesAndShapeGroups = new HashMap<>();
    // Get all the shape groups.
    for(Vertex vertex : vertices) {
      addAllNewGroupsFromVertex(vertex, groupType, shapesAndShapeGroups);
    }

    // Add all of the shape group connections.
    for(ShapeGroup shapeGroup : shapesAndShapeGroups.values()) {
      shapeGroup.populateNeighbors(shapesAndShapeGroups);
    }

    Set<ShapeGroup> set = new HashSet<>();
    // Remember, each group is represented multiple times because it is associated with multiple shapes.
    set.addAll(shapesAndShapeGroups.values());

    // Now to remove the groups that have nothing to do with the rectangle.
    List<ShapeGroup> shapeGroupsFromSet = new ArrayList<>(set);
    for(ShapeGroup shapeGroup : shapeGroupsFromSet) {
      boolean hasPointInside = false;
      for(Point[] points : shapeGroup.getShapePoints()) {
        for(Point point : points) {
          if(point.isGreaterThanOrEqual(low) && point.isLessThanOrEqual(high)) {
            hasPointInside = true;
            break;
          }
        }
        // Why check the rest of the point lists?
        // (that previous break didn't get out of this loop, just the inner loop)
        if(hasPointInside) {
          break;
        }
      }
      if(!hasPointInside) {
        set.remove(shapeGroup);
        // The neighbors need to not know that this shape exists anymore.
        // This shape isn't in the set, so no need to inform it.
        for(ShapeGroup neighbor : shapeGroup.getNeighbors()) {
          neighbor.neighborCircle.removeNeighbor(shapeGroup);
        }
      }
    }
    return set;
  }


  // The tricky thing here is that we want the exact order around the shape,
  // and without duplicate neighbors.
  // We're going to assume that a single shapeGroup does not "knot",
  // which is to say that each vertex represents just one place on the edge of the group,
  // the group doesn't exist in two separate parts connected by their points.
  // The possibilities on non-contiguous shapeGroups just give me a headache.
  public void populateNeighbors(Map<Shape, ShapeGroup> shapesAndShapeGroups) {
    // This gets meta pretty fast.
    // Hooo boy.
    // So fundamentally we need to walk around the shapeGroup and pick up neighbors as we go.
    // Only the Vertices referenced by the Shapes within the ShapeGroups actually know the neighbors.

    // Get a vertex that is on the edge of our shapeGroup.
    // That is a vertex that has at least one shape that is not in this group.
    Vertex startingVertex = null;
    for(Shape shape : shapes) {
      for(Vertex vertex : shape.getVertices()) {
        for(Shape vertexNeighbor : vertex.getWedges()) {
          if(!shapes.contains(vertexNeighbor)) {
            startingVertex = vertex;
            break;
          }
        }
        if(startingVertex != null) {
          break;
        }
      }
      if(startingVertex != null) {
        break;
      }
    }

    // Now we have a set of vertices on the edge of this group.
    // Let's figure out their order around the group.
    List<Vertex> verticesInOrder = new ArrayList<>();
    verticesInOrder.add(startingVertex);
    Vertex currentVertex = startingVertex;
    do {
      // Check currentVertex's shapes for the clockwise-most shape in our group.
      Shape[] vertexNeighborShapes = currentVertex.getWedges();
      int nextShapeIndex = lowestIndexOfItemInCollection(vertexNeighborShapes, shapes);
      // Okay, now we should have the next shape picked out.
      Shape nextShape = vertexNeighborShapes[nextShapeIndex];
      // And then we can pretty easily get the next vertex.
      // It's the next counterclockwise vertex on this shape.
      currentVertex = nextShape.getCounterclockwiseVertex(currentVertex);
      verticesInOrder.add(currentVertex);
    } while(currentVertex != startingVertex);

    // Whew.  Now we have all of the vertices in counterclockwise order around the group.
    // It's time to walk around these vertices and add their shapes to the circle of neighbors.
    List<Shape> shapesInOrderWithDuplicates = new ArrayList<>();
    for(Vertex vertex : verticesInOrder) {
      Shape[] vertexShapes = vertex.getWedges();
      // Find out where our shapes end, going around this vertex.
      int startingIndex = lowestIndexAboveCollectionStartingAt(vertex.getWedges(), shapes);
      // Now we have the counterclockwise start of shapes just after our group's shapes.
      // Add until we get back around to our group's shapes.
      // Since the eventual shapeGroups that these will turn into will be going into a set,
      // don't worry that there will be overlap with the previous and next vertices' shapes.
      for(int i = 0; i < vertexShapes.length; i++) {
        if(vertexShapes[startingIndex] != null) {
          shapesInOrderWithDuplicates.add(vertexShapes[startingIndex]);
        }
        startingIndex = (startingIndex + 1) % vertexShapes.length;
        if(shapes.contains(vertexShapes[startingIndex])) {
          break;
        }
      }
    }

    // And finally, adding the shape groups associated with these shapes in order should get what we wanted.
    for(Shape shape : shapesInOrderWithDuplicates) {
      neighborCircle.addNeighborInOrder(shapesAndShapeGroups.get(shape));
    }
  }

  private <T> int lowestIndexAboveCollectionStartingAt(T[] array, Collection<T> members) {
    int length = array.length;
    int result = 0;
    // Yup, these three loops could probably be compressed into one,
    // at the cost of readability.
    // I suspect there's no performance difference.

    // Find the first instance of one of the members.
    for(int i = 0; i < length; i++) {
      if(members.contains(array[result])) {
        break;
      }
      result = (result + 1) % length;
    }
    // Find the end of the streak of members.
    for(int i = 0; i < length; i++) {
      if(!members.contains(array[result])) {
        break;
      }
      result = (result + 1) % length;
    }
    // Find the first place that isn't one of the members.
    // It's possible that there are no places like that, in which case we'll return -1.
    for(int i = 0; i < length; i++) {
      if(!members.contains(array[result])) {
        return result;
      }
      result = (result + 1) % length;
    }
    // Uh-oh
    return -1;
  }

  private <T> int lowestIndexOfItemInCollection(T[] array, Collection<T> members) {
    int length = array.length;
    int result;
    // Find an member from the collection in the array.
    for(result = 0; result < length; result++) {
      if(members.contains(array[result])) {
        break;
      }
    }
    // Find an item before this streak of collection members.
    for(int i = 0; i < length; i++) {
      result = (result - 1 + length) % length;
      if(!members.contains(array[result])) {
        return (result + 1) % length;
      }
    }
    // Uh-oh.
    return -1;
  }


  private static void addAllNewGroupsFromVertex(Vertex vertex, ShapeGroupType groupType,
                                                Map<Shape, ShapeGroup> shapesAndShapeGroups) {
    Shape[] shapeArray = vertex.getWedges();
    Vwedge[] vwedges = vertex.getVwedges();
    for(int i = 0; i < Vertex.WEDGE_COUNT; i++) {

      // We'll only find all of this shape's neighbors at the vertex with the D or K type of V.
      if(vwedges[i] != null && (vwedges[i].v == V.D || vwedges[i].v == V.K)) {
        if(!shapesAndShapeGroups.containsKey(shapeArray[i])) {
          ShapeGroup tempShapeGroup;
          switch (groupType) {
            case STARS_AND_BALLS:
              int start = findBeginningOfVType(vwedges, i);
              tempShapeGroup = makeShapeGroupCounterclockwise(vwedges, shapeArray, start);
              for(Shape tempShape : tempShapeGroup.shapes) {
                shapesAndShapeGroups.put(tempShape, tempShapeGroup);
              }
              break;

            case SINGLE_SHAPES:
              tempShapeGroup = makeSingleShapeGroup(shapeArray[i]);
              shapesAndShapeGroups.put(shapeArray[i], tempShapeGroup);
              break;
          }
        }
      }
    }
  }

  private static int findBeginningOfVType(Vwedge[] vwedges, int location) {
    location = Vertex.normalizeWedgeNumber(location);
    V v = vwedges[location].v;
    for(int i = 0; i < Vertex.WEDGE_COUNT; i++) {
      location = Vertex.normalizeWedgeNumber(location - 1);
      if(vwedges[location] == null || vwedges[location].v != v) {
        return location + 1;
      }
    }
    // This means it's all the same V.  Doesn't really matter which one we return.
    return location;
  }

  private static ShapeGroup makeShapeGroupCounterclockwise(Vwedge[] vwedges, Shape[] shapeArray,
                                                           int location) {
    List<Shape> tempShapes = new ArrayList<>();
    location = Vertex.normalizeWedgeNumber(location);
    V v = vwedges[location].v;
    for(int i = 0; i < Vertex.WEDGE_COUNT; i++) {
      if(vwedges[location] != null && vwedges[location].v == v) {
        if(!tempShapes.contains(shapeArray[location])) {
          tempShapes.add(shapeArray[location]);
        }
      } else {
        // Found the end of this streak of shapes.
        return new ShapeGroup(tempShapes);
      }
      location = Vertex.normalizeWedgeNumber(location + 1);
    }
    // This means all the shapes were the same type.  Still valid.
    return new ShapeGroup(tempShapes);
  }

  private static ShapeGroup makeSingleShapeGroup(Shape shape) {
    return new ShapeGroup(Collections.singletonList(shape));
  }

  public enum ShapeGroupType {
    STARS_AND_BALLS,
    SINGLE_SHAPES
  }

}
