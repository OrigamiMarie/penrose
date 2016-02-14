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
  private Set<ShapeGroup> neighborShapeGroups;
  private ColoredShapeGroup coloredShapeGroup;
  private Point[] shapePoints;

  public ShapeGroup(List<Shape> shapes) {
    this.shapes = shapes;
    neighborShapeGroups = new HashSet<>();
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

  public void setColoredShapeGroup(ColoredShapeGroup coloredShapeGroup) {
    this.coloredShapeGroup = coloredShapeGroup;
  }

  public ColoredShapeGroup getColoredShapeGroup() {
    return coloredShapeGroup;
  }

  public Set<ShapeGroup> getNeighbors() {
    return neighborShapeGroups;
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
      populateNeighbors(shapeGroup, shapesAndShapeGroups);
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
        for(ShapeGroup neighbor : shapeGroup.neighborShapeGroups) {
          neighbor.neighborShapeGroups.remove(shapeGroup);
        }
      }
    }
    return set;
  }

  private static void populateNeighbors(ShapeGroup shapeGroup,
                                        Map<Shape, ShapeGroup> shapesAndShapeGroups) {
    for(Shape shape : shapeGroup.shapes) {
      for(Vertex vertex : shape.getVertices()) {
        for(Shape neighborOrSelfShape : vertex.getWedges()) {
          // If it's not self, then it's a neighbor.
          if(!shapeGroup.shapes.contains(neighborOrSelfShape)) {
            ShapeGroup neighborShapeGroup = shapesAndShapeGroups.get(neighborOrSelfShape);
            if(neighborShapeGroup != null) {
              if(shapeGroup.neighborShapeGroups.add(neighborShapeGroup)) {
                // Only if it got added to the set (because it wasn't there yet)
                // should we try adding it symmetrically.
                neighborShapeGroup.neighborShapeGroups.add(shapeGroup);
              }
            }
          }
        }
      }
    }
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
