package net.origamimarie.penrose.generation;

import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.output.SvgOutput;
import org.apache.commons.collections4.ListUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

@Slf4j
public class Vertex implements Comparable<Vertex> {

  public static final int WEDGE_COUNT = 10;

  private static Random rand = new Random();
  private static List<Vwedge[]> allVwedgeConfigurations;

  static {
    // There are a fixed number of possible VWedge configurations.
    // There are not quite 54 of them:
    //    7 configurations of shapes around a vertex,
    //    10 rotations each,
    //    but 2 of those have 5-part symmetry so they only multiply by two each.
    // So, pre-calculate all of those vs configurations since it's a small set.
    allVwedgeConfigurations = new ArrayList<>(52);

    List<Vwedge[]> baseVwedges = new ArrayList<>(2);
    baseVwedges.add(new Vwedge[]{Vwedge.D0, Vwedge.D1, Vwedge.D0, Vwedge.D1, Vwedge.D0, Vwedge.D1, Vwedge.D0, Vwedge.D1, Vwedge.D0, Vwedge.D1});
    baseVwedges.add(new Vwedge[]{Vwedge.K0, Vwedge.K1, Vwedge.K0, Vwedge.K1, Vwedge.K0, Vwedge.K1, Vwedge.K0, Vwedge.K1, Vwedge.K0, Vwedge.K1});
    for(Vwedge[] baseVwedge : baseVwedges) {
      List<Vwedge> baseList = new ArrayList<>();
      baseList.addAll(Arrays.asList(baseVwedge));
      for(int i = 0; i < 2; i++) {
        List<Vwedge> copy = new ArrayList<>();
        copy.addAll(baseList);
        Collections.rotate(copy, i);
        Vwedge[] tempVwedge = new Vwedge[10];
        System.arraycopy(copy.toArray(tempVwedge), 0, tempVwedge, 0, 10);
        allVwedgeConfigurations.add(tempVwedge);
      }
    }

    baseVwedges = new ArrayList<>(5);
    baseVwedges.add(new Vwedge[]{Vwedge.F0, Vwedge.F1, Vwedge.F2, Vwedge.F3, Vwedge.F4, Vwedge.F5, Vwedge.N0, Vwedge.N1, Vwedge.L0, Vwedge.L1});
    baseVwedges.add(new Vwedge[]{Vwedge.D0, Vwedge.D1, Vwedge.D0, Vwedge.D1, Vwedge.D0, Vwedge.D1, Vwedge.L0, Vwedge.L1, Vwedge.N0, Vwedge.N1});
    baseVwedges.add(new Vwedge[]{Vwedge.G0, Vwedge.K0, Vwedge.K1, Vwedge.K0, Vwedge.K1, Vwedge.E0, Vwedge.M0, Vwedge.M1, Vwedge.M2, Vwedge.M3});
    baseVwedges.add(new Vwedge[]{Vwedge.D0, Vwedge.D1, Vwedge.L0, Vwedge.L1, Vwedge.N0, Vwedge.N1, Vwedge.L0, Vwedge.L1, Vwedge.N0, Vwedge.N1});
    baseVwedges.add(new Vwedge[]{Vwedge.M0, Vwedge.M1, Vwedge.M2, Vwedge.M3, Vwedge.M0, Vwedge.M1, Vwedge.M2, Vwedge.M3, Vwedge.G0, Vwedge.E0});
    // There are a bunch of steps of indirection here so that we don't end up modifying the same copy repeatedly.
    // Probably don't need this many steps of indirection, but this should be pretty certain,
    // and we're only doing it once.
    for(Vwedge[] baseVwedge : baseVwedges) {
      List<Vwedge> baseList = new ArrayList<>();
      baseList.addAll(Arrays.asList(baseVwedge));
      for(int i = 0; i < 10; i++) {
        List<Vwedge> copy = new ArrayList<>();
        copy.addAll(baseList);
        Collections.rotate(copy, i);
        Vwedge[] tempVwedge = new Vwedge[10];
        System.arraycopy(copy.toArray(tempVwedge), 0, tempVwedge, 0, 10);
        allVwedgeConfigurations.add(tempVwedge);
      }
    }
  }

  private Shape[] wedges;
  private Vwedge[] vwedges;
  private List<Vwedge[]> currentPossibleVwedges;
  private int openWedges;
  private boolean dead;
  private Vertex replacement = null;

  private Point location = null;
  private Set<Vertex> allTheLiveVertices = null;
  private Point.PointSet pointSet = null;
  private Map<Point, Vertex> pointsToVertices = null;

  public Vertex() {
    openWedges = WEDGE_COUNT;
    wedges = new Shape[WEDGE_COUNT];
    vwedges = new Vwedge[]{null, null, null, null, null, null, null, null, null, null};
    dead = false;
    currentPossibleVwedges = new ArrayList<>(allVwedgeConfigurations);
    allTheLiveVertices = new HashSet<>();
    pointSet = new Point.PointSet();
    pointsToVertices = new HashMap<>();
  }

  public boolean isFull() {
    return openWedges == 0;
  }

  public boolean isDead() {
    return dead;
  }

  public Shape[] getWedges() {
    Shape[] wedgesCopy = new Shape[WEDGE_COUNT];
    System.arraycopy(wedges, 0, wedgesCopy, 0, WEDGE_COUNT);
    return wedgesCopy;
  }

  public Vwedge[] getVwedges() {
    Vwedge[] vwedgesCopy = new Vwedge[WEDGE_COUNT];
    System.arraycopy(vwedges, 0, vwedgesCopy, 0, WEDGE_COUNT);
    return vwedgesCopy;
  }

  public Vertex calculateReplacement() {
    if(!dead) {
      return this;
    } else {
      return replacement.calculateReplacement();
    }
  }

  // The point here is to make queueing nice.
  // The fewer open wedges, the more interested we are in trying to fill it in.
  @Override
  public int compareTo(Vertex v) {
    return openWedges - v.openWedges;
  }

  // Find an open slot, figure out what to put in it, and drop it in.
  public void addRandomShape() {
    if(isFull() || currentPossibleVwedges.size() == 0) {
      return;
    }
    // Start somewhere random.
    int seekStart = rand.nextInt(10);
    // Find an open spot.
    for(int i = 0; i < 10; i++) {
      if(vwedges[seekStart] == null) {
        break;
      } else {
        seekStart = normalizeWedgeNumber(seekStart+1);
      }
    }

    // Okay, now we have a random empty space.
    // So next we need a shape to drop in.
    // Pick a random item from currentPossibleVwedges and work it out from there.
    int randomNumber = rand.nextInt(currentPossibleVwedges.size());
    Vwedge vwedge = currentPossibleVwedges.get(randomNumber)[seekStart];
    addShape(Shape.makeNew(vwedge.associatedShape), vwedge, seekStart, true);
  }

  public void addShape(Shape shape, Vwedge vwedge, int vwedgeLocation, boolean autoFillAll) {
    // Help the shape figure out its orientation.
    shape.setOrientation(vwedge, vwedgeLocation);
    // Add the shape to this vertex, which also adds this vertex to the shape and recursively merges vertices.
    for(Vertex vertex : shape.getVertices()) {
      vertex.allTheLiveVertices = this.allTheLiveVertices;
      vertex.pointSet = this.pointSet;
      vertex.pointsToVertices = this.pointsToVertices;
    }
    addShapeWithOrientation(shape, vwedge);

    // This prevents deeply nested autoFillAll loops,
    // which tend to cause problems and are unnecessary.
    if(autoFillAll) {
      autoFillAllTheShapes();
    }
  }

  // Only call this if the orientation is set on the shape already.
  // Probably an NPE if you mess up.
  public void addShapeWithOrientation(Shape shape, Vwedge vwedge) {
    // Need to call replace on just this one,
    // so that the location can get set for all of the vertices on this shape.
    mergeVertexIntoThis(shape.getVertex(vwedge));
    shape.replaceVertex(shape.getVertex(vwedge), this);
    for(Vertex vertex : shape.getVertices()) {
      vertex.findLocationCopyAndMerge();
    }
    /*
    // The shape already has a vertex in the spot where this one should go.
    // So let's get that vertex, merge it to this, and replace it.
    Vertex previousVertex = shape.getVertex(vwedge).calculateReplacement();
    if(previousVertex != this) {
      mergeVertexIntoThis(previousVertex);
    }*/
  }

  private void findLocationCopyAndMerge() {
    Point similarExisting = pointSet.getSimilarPoint(location);
    if(similarExisting != null && similarExisting != location) {
      // This means there's a vertex to merge with.
      Vertex vertexToMergeWith = pointsToVertices.get(similarExisting);
      // This is signing up to be eaten (and declared dead) by that vertex.
      vertexToMergeWith.mergeVertexIntoThis(this);
    } else {
      // This point isn't in our lists yet, so add it.
      pointSet.addPoint(location);
      allTheLiveVertices.add(this);
      pointsToVertices.put(location, this);
    }
  }

  private void mergeVertexIntoThis(Vertex vertex) {
    // It's dead.  This ate it.
    vertex.dead = true;
    vertex.replacement = this;
    allTheLiveVertices.remove(vertex);

    Set<Shape> shapesToReplaceVerticesIn = new HashSet<>();
    for(int i = 0; i < WEDGE_COUNT; i++) {
      if(vertex.wedges[i] != null && this.wedges[i] != null && vertex.wedges[i] != this.wedges[i]) {
        // TODO remove when debugged.
        dumpToSvgDebug();
        throw new IllegalArgumentException("Trying to merge two vertices that have incompatible wedges.  ");
      }
      if(vertex.wedges[i] != null) {
        this.wedges[i] = vertex.wedges[i];
        openWedges--;
        this.vwedges[i] = vertex.vwedges[i];
        shapesToReplaceVerticesIn.add(wedges[i]);
      }
    }
    for(Shape shape : shapesToReplaceVerticesIn) {
      shape.replaceVertex(vertex, this.calculateReplacement());
    }

    // Only possibleVwedges that worked with both.
    this.currentPossibleVwedges = ListUtils.intersection(this.currentPossibleVwedges, vertex.currentPossibleVwedges);
  }

  public void dumpToSvgDebug() {
    File file = new File("/Users/mariep/personalcode/penrose/foo.svg");
    List<Point> allVertexPoints = new ArrayList<>();
    for(Vertex vertex : allTheLiveVertices) {
      allVertexPoints.add(vertex.getLocation());
    }
    try {
      SvgOutput.pointListsToSvgFile(file, getAllShapePoints(), 40, Color.MAGENTA, 0.4, true, allVertexPoints);
    } catch (Exception ignored) {}

  }

  // So it turns out that we might be able to completely avoid merging accidents by
  // always just auto-filling vertices in order of when they were first made.
  // Or something like that.
  private void autoFillAllTheShapes() {
    List<Shape> addedShapes;
    if(dead) {
      // There's just no point in auto-filling a system that you're no longer a part of.
      // Some other live vertex will take care of that.
      return;
    }
    do {
      // Get all of the live vertices connected to this one,
      // and auto-fill them.
      PriorityQueue<Vertex> queue = new PriorityQueue<>(getAllLiveNonFullVertices());

      addedShapes = new ArrayList<>();
      while(queue.size() > 0) {
        Vertex tempVertex = queue.remove();
        // This vertex may have been killed or filled within this loop.
        if(!tempVertex.isDead() && !tempVertex.isFull()) {
          List<Shape> tempAddedShapes = tempVertex.autoFillShapes();
          addedShapes.addAll(tempAddedShapes);

        }
      }
    } while(addedShapes.size() > 0 && !dead);
  }

  private List<Shape> autoFillShapes() {
    // The obvious thing is to see if there is just one item in currentPossibleVwedges.
    // But there's something a little more subtle too.
    // If all of the currentPossibleVwedges have the same Vwedge in an as-yet-unfilled spot,
    // that spot needs to get filled with that VWedge.
    Vwedge[] certainVwedges = new Vwedge[WEDGE_COUNT];
    boolean[] justOneVwedge = new boolean[WEDGE_COUNT];
    for(int i = 0; i < certainVwedges.length; i++) {
      certainVwedges[i] = null;
      // We only care if there isn't already a vwedge here.
      justOneVwedge[i] = (vwedges[i] == null);
      if(justOneVwedge[i]) {
        for(Vwedge[] tempVwedges : currentPossibleVwedges) {
          if(certainVwedges[i] == null) {
            certainVwedges[i] = tempVwedges[i];
          } else if(certainVwedges[i] != tempVwedges[i]) {
            // This means there are two possibilities for this vwedge,
            // which means it won't get auto-filled.
            justOneVwedge[i] = false;
            break;
          }
        }
      }
    }

    List<Shape> addedShapes = new ArrayList<>();
    Vertex thisOrReplacement;
    for(int i = 0; i < WEDGE_COUNT; i++) {
      thisOrReplacement = calculateReplacement();
      // Need one last check of vwedges[i] there because we may have filled it just now.
      if(justOneVwedge[i] && vwedges[i] == null) {
        Vwedge vwedge = certainVwedges[i];
        Shape shape = Shape.makeNew(vwedge.associatedShape);
        addedShapes.add(shape);
        thisOrReplacement.addShape(shape, vwedge, i, false);
      }
    }
    return addedShapes;
  }

  // V is which vertex we are in relation to this Shape.
  public void putShapeInWedges(Shape shape, V v) {
    Orientation orientation = shape.getOrientation();
    for(Vwedge vwedge : Vwedge.getVwedges(v)) {
      int i = orientation.getWedgeLocation(vwedge);
      wedges[i] = shape;
      openWedges--;
      vwedges[i] = vwedge;
    }
    recalculatePossibleVwedges();
  }

  // This figures out which of the remaining currentPossibleVwedges
  // are now inconsistent with the vwedges, and removes them.
  private void recalculatePossibleVwedges() {
    for(int i = 0; i < currentPossibleVwedges.size(); i++) {
      for(int j = 0; j < 10; j++) {
        if(vwedges[j] != null && vwedges[j] != currentPossibleVwedges.get(i)[j]) {
          currentPossibleVwedges.remove(i);
          i--;
          break;
        }
      }
    }
  }

  public void setLocation(Point location, Vertex anyLocatedVertex) {
    if(this.location == null) {
      this.location = location;
      if(anyLocatedVertex != null) {
        this.allTheLiveVertices = anyLocatedVertex.allTheLiveVertices;
        this.pointSet = anyLocatedVertex.pointSet;
        this.pointsToVertices = anyLocatedVertex.pointsToVertices;
      }
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

  public List<Point[]> getAllShapePoints() {
    Set<Shape> visitedShapes = new HashSet<>();
    List<Point[]> shapePoints = new ArrayList<>();
    for(Vertex vertex : allTheLiveVertices) {
      for(Shape shape : vertex.wedges) {
        if(shape != null && visitedShapes.add(shape)) {
          shapePoints.add(shape.getShapePoints());
        }
      }
    }
    return shapePoints;
  }

  public Set<Vertex> getAllLiveNonFullVertices() {
    Set<Vertex> liveNonFullVertices = new HashSet<>();
    for(Vertex vertex : allTheLiveVertices) {
      if(!vertex.isFull()) {
        liveNonFullVertices.add(vertex);
      }
    }
    return liveNonFullVertices;
  }

  public Set<Vertex> getAllLiveVertices() {
    return allTheLiveVertices;
  }

}
