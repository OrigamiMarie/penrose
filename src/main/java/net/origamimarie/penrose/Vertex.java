package net.origamimarie.penrose;

import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Vertex {

  public static final int WEDGE_COUNT = 10;

  private static Random rand = new Random();
  private static List<V[]> allVsConfigurations;

  static {
    // There are a fixed number of possible Vs configurations.
    // There are not quite 52 of them:
    //    7 configurations of shapes around a vertex,
    //    10 rotations each,
    //    but 2 of those have 10-part symmetry so they don't multiply out.
    // So, pre-calculate all of those vs configurations since it's a small set.
    allVsConfigurations = new ArrayList<>(52);
    allVsConfigurations.add(new V[]{V.D, V.D, V.D, V.D, V.D, V.D, V.D, V.D, V.D, V.D});
    allVsConfigurations.add(new V[]{V.K, V.K, V.K, V.K, V.K, V.K, V.K, V.K, V.K, V.K});
    List<V[]> baseVs = new ArrayList<>(5);
    baseVs.add(new V[]{V.F, V.F, V.F, V.F, V.F, V.F, V.N, V.N, V.L, V.L});
    baseVs.add(new V[]{V.D, V.D, V.D, V.D, V.D, V.D, V.L, V.L, V.N, V.N});
    baseVs.add(new V[]{V.G, V.K, V.K, V.K, V.K, V.E, V.N, V.N, V.L, V.L});
    baseVs.add(new V[]{V.D, V.D, V.L, V.L, V.N, V.N, V.L, V.L, V.N, V.N});
    baseVs.add(new V[]{V.M, V.M, V.M, V.M, V.M, V.M, V.M, V.M, V.G, V.E});
    // There are a bunch of steps of indirection here so that we don't end up modifying the same copy repeatedly.
    // Probably don't need this many steps of indirection, but this should be pretty certain,
    // and we're only doing it once.
    for(V[] baseV : baseVs) {
      List<V> baseList = new ArrayList<>();
      baseList.addAll(Arrays.asList(baseV));
      for(int i = 0; i < 10; i++) {
        List<V> copy = new ArrayList<>();
        copy.addAll(baseList);
        Collections.rotate(copy, i);
        V[] tempV = new V[10];
        System.arraycopy(copy.toArray(tempV), 0, tempV, 0, 10);
        allVsConfigurations.add(tempV);
      }
    }
  }

  private Shape[] wedges;
  private V[] vs;
  private List<V[]> currentPossibleVs;
  private Point location = null;
  private boolean dead;

  public Vertex() {
    wedges = new Shape[WEDGE_COUNT];
    vs = new V[]{null, null, null, null, null, null, null, null, null, null};
    dead = false;
    currentPossibleVs = new ArrayList<>(allVsConfigurations);
  }

  public void addShape(Shape shape, V v, int clockwiseMostWedge) {
    // Help the shape figure out its orientation.
    shape.setOrientation(v, clockwiseMostWedge);
    // Now we can rely on that orientation for the rest of this.
    addShapeWithOrientation(shape, v);
  }

  // Find an open slot, chase it clockwise, figure out what to put in it, and drop it in.
  public boolean addRandomShape() {
    // Start somewhere random.
    int seekStart = rand.nextInt(10);
    // Find an open spot.
    for(int i = 0; i < 10; i++) {
      if(vs[seekStart] == null) {
        break;
      } else {
        seekStart = normalizeWedgeNumber(seekStart+1);
      }
    }
    // That may not be the most clockwise empty place.
    for(int i = 0; i < 10; i++) {
      if(vs[normalizeWedgeNumber(seekStart-1)] == null) {
        seekStart = normalizeWedgeNumber(seekStart-1);
      } else {
        break;
      }
    }
    // It's possible that this shape is full.
    // If so, don't try to add a new shape.
    if(vs[seekStart] != null) {
      return false;
    }

    // Okay, now we have a random most-clockwise empty space.
    // So next we need a shape to drop in.
    // Pick a random item from currentPossibleVs and work it out from there.
    int randomNumber = rand.nextInt(currentPossibleVs.size());
    V v = currentPossibleVs.get(randomNumber)[seekStart];
    addShape(Shape.makeNew(v.associatedShape), v, seekStart);
    return true;
  }

  // Only call this if the orientation is set on the shape already.
  // Probably an NPE if you mess up.
  public void addShapeWithOrientation(Shape shape, V v) {
    // The shape already has a vertex in the spot where this one should go.
    // So let's get that vertex, merge it to this, and replace it.
    Vertex previousVertex = shape.getVertex(v);
    // By some magic, this may be the same vertex.
    if(previousVertex != this) {
      mergeVertexIntoThis(previousVertex);
      shape.setVertex(this, v);
    }
  }

  private void mergeVertexIntoThis(Vertex vertex) {
    // It's dead.  This ate it.
    vertex.dead = true;
    for(int i = 0; i < WEDGE_COUNT; i++) {
      if(vertex.wedges[i] != null && this.wedges[i] != null) {
        throw new IllegalArgumentException("Trying to merge two vertices that have incompatible wedges.  ");
      }
      if(vertex.wedges[i] != null) {
        this.wedges[i] = vertex.wedges[i];
        this.vs[i] = vertex.vs[i];
      }
    }
    // Only vertices that worked with both.
    // I'm not sure this excludes enough, but I think it does.
    this.currentPossibleVs = ListUtils.intersection(this.currentPossibleVs, vertex.currentPossibleVs);
    if(this.location == null) {
      this.location = vertex.location;
    }
  }

  // V is which vertex we are in relation to this Shape.
  public void putShapeInWedges(Shape shape, V v) {
    Orientation orientation = shape.getOrientation();
    for(int i : orientation.getWedgeLocations(v)) {
      wedges[i] = shape;
      vs[i] = v;
    }
    recalculatePossibleVs();
  }

  // This figures out which of the remaining currentPossibleVs
  // are now inconsistent with the vs, and removes them.
  private void recalculatePossibleVs() {
    for(int i = 0; i < currentPossibleVs.size(); i++) {
      for(int j = 0; j < 10; j++) {
        if(vs[j] != null && vs[j] != currentPossibleVs.get(i)[j]) {
          currentPossibleVs.remove(i);
          i--;
          break;
        }
      }
    }
  }

  public void setLocation(Point location) {
    if(this.location == null) {
      this.location = location;
    }
  }

  public Point getLocation() {
    return location;
  }

  public static int normalizeWedgeNumber(int i) {
    // Don't trust any modulus operator interactions with negative numbers.
    while(i < 0) {
      i = i + WEDGE_COUNT;
    }
    return i % WEDGE_COUNT;
  }

  public List<Point[]> getAllShapePoints(List<Shape> visitedShapes) {
    List<Point[]> points = new ArrayList<>();
    for(Shape shape : wedges) {
      if(shape != null && !visitedShapes.contains(shape)) {
        visitedShapes.add(shape);
        points.addAll(shape.getAllShapePoints(visitedShapes));
      }
    }
    return points;
  }

}
