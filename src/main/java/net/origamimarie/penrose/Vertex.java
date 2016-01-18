package net.origamimarie.penrose;


import java.util.List;

import static net.origamimarie.penrose.Shape.V;


/*
  0 is straight east, and numbers proceed counterclockwise up through 9.
  Each shape vertex occupies a set number of vertex zones depending on its angle.
 */
public class Vertex {
  private Shape[] shapeSlots;
  private Point location;

  public Vertex() {
    shapeSlots = new Shape[10];
    location = null;
  }

  public void mergeVertexIntoSelf(Vertex other) {
    if(other != this) {
      for(int i = 0; i < 10; i++) {
        if(shapeSlots[i] != null && other.shapeSlots[i] != null) {
          throw new IllegalArgumentException("Tried to merge two vertices with different shapes in the same slot!  ");
        }
        if(other.shapeSlots[i] != null) {
          this.shapeSlots[i] = other.shapeSlots[i];
        }
      }
    }
  }

  public void setLocation(Point l) {
    // We're using this as a marker that the location has been set,
    // so that we can traverse the whole network of Vertex objects exactly once.
    if(location == null) {
      location = l;
      // We need to tell the shape its orientation,
      // which is going to be the direction of its most clockwise slot.
      // That means some extra entertainment for the first shape.
      Shape firstShape = shapeSlots[0];
      Shape currentShape = firstShape;
      int i = 0;
      while(currentShape == firstShape) {
        i = i + 1;
        currentShape = getShapeFromSlot(i);
      }
      currentShape.setLocation(this, l, normalizeI(i));
      while(currentShape != firstShape) {
        while(getShapeFromSlot(i) == currentShape) {
          i = i + 1;
        }
        currentShape = getShapeFromSlot(i);
        currentShape.setLocation(this, l, normalizeI(i));
      }
    }
  }

  public void getAllShapePoints(List<Shape> shapesAlreadyGotten, List<Point[]> pointLists) {
    for(Shape shape : shapeSlots) {
      shape.getAllShapePoints(shapesAlreadyGotten, pointLists);
    }
  }

  public void putShapeCounterClockwise(Shape shape, int startingSlot, V v) {
    for(int i = startingSlot; i < startingSlot + v.vertexSize; i++) {
      if(getShapeFromSlot(i) != null) {
        throw new IllegalArgumentException(String.format("There was already a %s in slot %d",
                getShapeFromSlot(i).getShapeTypeName(), i));
      }
    }
    // Okay, we're clear for installation.
    installShapeCounterclockwiseStartingHere(shape, startingSlot, v);
  }

  public void putShapeClockwise(Shape shape, int startingSlot, V v) {
    // Just back it around the clock, then let the counterclockwise version take care of it.
    int i = startingSlot - v.vertexSize;
    if(i < 0) {
      i = i+10;
    }
    putShapeCounterClockwise(shape, i, v);
  }

  public void putShapeCounterclockwiseOf(Shape shape, Shape of, V v) {
    // Find the slot just counterclockwise from of.
    int startingSlot = -1;
    boolean found = false;
    for(int i = 0; i < 20; i++) {
      if(getShapeFromSlot(i) == of) {
        found = true;
      }
      if(found && getShapeFromSlot(i) != of) {
        startingSlot = normalizeI(i);
        break;
      }
    }
    // If we found the location for of, it would be normalized and this wouldn't be -1.
    if(startingSlot == -1) {
      throw new IllegalArgumentException(String.format("Shape %s not found in Vertex %s",
              of.toString(), this.toString()));
    }
    putShapeCounterClockwise(shape, startingSlot, v);
  }

  // This feels pretty duplicate, I haven't figured out how to get rid of that problem yet.
  public void putShapeClockwiseOf(Shape shape, Shape of, V v) {
    // Find the slot just clockwise from of.
    int startingSlot = -1;
    boolean found = false;
    for(int i = 19; i >= 0; i--) {
      if(getShapeFromSlot(i) == of) {
        found = true;
      }
      if(found && getShapeFromSlot(i) != of) {
        startingSlot = normalizeI(i);
      }
    }
    // If we found the location for of, it would be normalized and this wouldn't be -1.
    if(startingSlot == -1) {
      throw new IllegalArgumentException(String.format("Shape %s not found in Vertex %s",
              of.toString(), this.toString()));
    }
    putShapeClockwise(shape, startingSlot, v);
  }

  // Internal only.  This assumes you've already figured out a valid location.
  private void installShapeCounterclockwiseStartingHere(Shape shape, int startingSlot, V v) {
    // Do basic adding to slots.
    for(int i = startingSlot; i < startingSlot + v.vertexSize; i++) {
      shapeSlots[normalizeI(i)] = shape;
    }
    // Shape needs to know where this Vertex is on it.
    shape.setVertex(this, v);
    // Now we need to join this shape with its neighbors if it has any.
    Shape clockwiseShape = getShapeFromSlot(startingSlot-1);
    if(clockwiseShape != null) {
      shape.setNeighborClockwise(clockwiseShape, this);
    }
    Shape counterclockwiseShape = getShapeFromSlot(startingSlot+v.vertexSize+1);
    if(counterclockwiseShape != null) {
      shape.setNeighborCounterClockwise(counterclockwiseShape, this);
    }
  }

  // This takes care of overrunning the boundaries either negative or excessively positive.
  private int normalizeI(int i) {
    // Don't trust any modulus operator interactions with negative numbers.
    while(i < 0) {
      i = i + 10;
    }
    return i % 10;
  }

  private Shape getShapeFromSlot(int i) {
    return shapeSlots[normalizeI(i)];
  }

}
