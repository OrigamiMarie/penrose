package net.origamimarie.penrose;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

@Slf4j
public class Vertex implements Comparable<Vertex> {

  public static final int WEDGE_COUNT = 10;

  private static Random rand = new Random();
  private static List<Vwedge[]> allVwedgeConfigurations;

  static {
    // There are a fixed number of possible Vs configurations.
    // There are not quite 54 of them:
    //    7 configurations of shapes around a vertex,
    //    10 rotations each,
    //    but 2 of those have 5-part symmetry so they don't multiply out.
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
  private Point location = null;
  private boolean dead;

  public Vertex() {
    openWedges = WEDGE_COUNT;
    wedges = new Shape[WEDGE_COUNT];
    vwedges = new Vwedge[]{null, null, null, null, null, null, null, null, null, null};
    dead = false;
    currentPossibleVwedges = new ArrayList<>(allVwedgeConfigurations);
  }

  public boolean isFull() {
    return openWedges == 0;
  }

  public boolean isDead() {
    return dead;
  }

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
    addShape(Shape.makeNew(vwedge.associatedShape), vwedge, seekStart, 0);
  }


  public void addShape(Shape shape, Vwedge vwedge, int vwedgeLocation, int autoFillStackCount) {
    // Basic things that need to happen in here:
    //  *  Determine what orientation this shape has.
    //  *  Add the shape to the wedges and vwedges.
    //  *  Add this vertex to the shape (the shape should be "fresh" and not have other complicated vertices yet).
    //  *  Merge the shape's vertices with other shape vertices as needed (recursively -- each merging should check for other possible necessary mergings).
    //  *  Figure out and execute auto-fill (which involves calling addShape again).

    // Help the shape figure out its orientation.
    shape.setOrientation(vwedge, vwedgeLocation);
    // Add the shape to this vertex, which also adds this vertex to the shape and recursively merges vertices.
    addShapeWithOrientation(shape, vwedge);

    autoFillAllTheShapes(autoFillStackCount + 1);
  }

  // Only call this if the orientation is set on the shape already.
  // Probably an NPE if you mess up.
  public void addShapeWithOrientation(Shape shape, Vwedge vwedge) {
    // The shape already has a vertex in the spot where this one should go.
    // So let's get that vertex, merge it to this, and replace it.
    Vertex previousVertex = shape.getVertex(vwedge);
    if(previousVertex != this) {
      mergeVertexIntoThis(previousVertex);
    }
  }

  private void mergeVertexIntoThis(Vertex vertex) {
    // It's dead.  This ate it.
    vertex.dead = true;
    Set<Shape> shapesToReplaceVerticesIn = new HashSet<>();
    for(int i = 0; i < WEDGE_COUNT; i++) {
      if(vertex.wedges[i] != null && this.wedges[i] != null && vertex.wedges[i] != this.wedges[i]) {
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
      shape.replaceVertex(vertex, this);
    }
    // Only possibleVs that worked with both.
    this.currentPossibleVwedges = ListUtils.intersection(this.currentPossibleVwedges, vertex.currentPossibleVwedges);
    if(this.location == null) {
      this.location = vertex.location;
    }

    // Now go find all of the other vertices that need merging.
    // Every time you merge a vertex, there is a chance that there will be news about
    // another pair of vertices that are actually the same vertex.
    Shape clockwise;
    Shape counterclockwise;
    Vertex v1;
    Vertex v2;
    for(int i = 0; i < WEDGE_COUNT; i++) {
      clockwise = wedges[i];
      counterclockwise = wedges[normalizeWedgeNumber(i+1)];
      // This means two different shapes are next to each other.
      if(clockwise != null && counterclockwise != null && clockwise != counterclockwise) {
        // They should have this vertex and one other vertex in common.
        v1 = clockwise.getVertexCounterclockwiseOf(this);
        v2 = counterclockwise.getVertexClockwiseOf(this);
        // If these aren't the same yet, they should be merged.
        if(v1 == null || v2 == null) {
          log.debug("oh-oh");
        }
        if(v1 != v2) {
          v1.mergeVertexIntoThis(v2);
        }
      }
    }
  }

  private void autoFillAllTheShapes(int autoFillStackCount) {
    // Okay, so we're having a stack overflow problem.
    // The quickest, easiest way to deal with that is to
    // just return back if this is above a certain stack frame count.
    // The iterative algorithm will take care of the rest.
    /*if(autoFillStackCount > 400) {
      return;
    }*/

    List<Shape> addedShapes;
    if(dead) {
      // There's just no point in auto-filling a system that you're no longer a part of.
      // Some other live vertex will take care of that.
      return;
    }
    do {
      // Get all of the live vertices connected to this one,
      // and auto-fill them.
      Set<Vertex> allLiveVertices = getAllLiveNonFullVertices();
      Queue<Vertex> queue = new PriorityQueue<>(allLiveVertices);

      addedShapes = new ArrayList<>();
      while(queue.size() > 0) {
        Vertex tempVertex = queue.remove();
        // This vertex may have been killed or filled within this loop.
        if(!tempVertex.isDead() && !tempVertex.isFull()) {
          List<Shape> tempAddedShapes = tempVertex.autoFillShapes(autoFillStackCount++);
          addedShapes.addAll(tempAddedShapes);
          for(Shape tempAddedShape : tempAddedShapes) {
            for(Vertex tempAddedVertex : tempAddedShape.getVertices()) {
              if(!allLiveVertices.contains(tempAddedVertex)) {
                allLiveVertices.add(tempAddedVertex);
                queue.add(tempAddedVertex);
              }
            }
          }
        }
      }
    } while(addedShapes.size() > 0);
  }

  private List<Shape> autoFillShapes(int autoFillStackCount) {
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

    // TODO remove this it's just for debugging
    /*int countOfJustOne = 0;
    for(boolean b : justOneVwedge) {
      countOfJustOne += (b ? 1 : 0);
    }
    if(countOfJustOne > 0) {
      try {
        if(location != null) {
          SvgOutput.pointListsToSvgFile(file, getAllShapePoints(new ArrayList<Shape>()), 20, null);
        }
      } catch (IOException ignored) {
        log.debug("exception here ", ignored);
      }
      log.debug("Will autofill at least one shape.  ");
    }*/
    List<Shape> addedShapes = new ArrayList<>();
    // We may stop this loop partway through due to self being dead.
    // Since the shapes we're adding are doing their own recursive vertex merging,
    // this vertex may lose the competition mid-loop.
    for(int i = 0; i < WEDGE_COUNT && !dead; i++) {
      // Need one last check of vwedges[i] there because we may have filled it just now.
      // Also, we may be dead.
      if(justOneVwedge[i] && vwedges[i] == null && !dead) {
        Vwedge vwedge = certainVwedges[i];
        Shape shape = Shape.makeNew(vwedge.associatedShape);
        addedShapes.add(shape);
        addShape(shape, vwedge, i, autoFillStackCount);
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
  // are now inconsistent with the vs, and removes them.
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

  public List<Point[]> getAllShapePoints() {
    // Recursion is just not going to work here.  Too many stack frames.
    Set<Shape> visitedShapes = new HashSet<>();
    List<Point[]> shapePoints = new ArrayList<>();

    Set<Shape> currentShapes = new HashSet<>();
    currentShapes.addAll(Arrays.asList(wedges));
    currentShapes.remove(null);
    do {
      List<Shape> shapesLastRound = new ArrayList<>(currentShapes);
      currentShapes.clear();
      for(Shape shape : shapesLastRound) {
        shapePoints.add(shape.getShapePoints());
        for(Vertex vertex : shape.getVertices()) {
          for(Shape tempShape : vertex.wedges) {
            if(tempShape != null && visitedShapes.add(tempShape)) {
              currentShapes.add(tempShape);
            }
          }
        }
      }
    } while(currentShapes.size() > 0);

    return shapePoints;
  }

  public Vertex findAnyLiveNonFullVertex() {
    return findAnyLiveVertex(true);
  }

  private Vertex findAnyLiveVertex(boolean requiredNonFull) {
    // TODO get rid of this quick hack, it was just a modified copy from getAll.
    if(!dead && ((requiredNonFull && !isFull()) || !requiredNonFull)) {
      return this;
    }

    Set<Vertex> allVertices = new HashSet<>();
    Set<Vertex> verticesAddedLastRound = new HashSet<>();
    verticesAddedLastRound.add(this);
    while(verticesAddedLastRound.size() > 0) {
      List<Vertex> verticesToProcess = new ArrayList<>(verticesAddedLastRound);
      verticesAddedLastRound.clear();
      // Owie this is quite the nest.
      for(Vertex vertex : verticesToProcess) {
        for(Shape shape : vertex.wedges) {
          if(shape != null) {
            for(Vertex tempVertex : shape.getVertices()) {
              // Note:  we're doing the dead check here because dead nodes aren't necessary bridges to live nodes,
              // but we're not doing the full check because full nodes might be necessary bridges to other non-full nodes.
              if(!tempVertex.isDead()) {
                if(!requiredNonFull || !tempVertex.isFull()) {
                  return tempVertex;
                }
                if(allVertices.add(tempVertex)) {
                  verticesAddedLastRound.add(tempVertex);
                }
              }
            }
          }
        }
      }
    }
    return null;

    /*
    verticesTried.add(this);
    if(!dead && ((requiredNonFull && !isFull()) || !requiredNonFull)) {
      return this;
    } else {
      for(Shape shape : wedges) {
        if(shape != null) {
          for(Vertex tempVertex : shape.getVertices()) {
            if(!verticesTried.contains(tempVertex)) {
              tempVertex = tempVertex.findAnyLiveNonFullVertex();
              if(tempVertex != null) {
                return tempVertex;
              }
            }
          }
        }
      }
    }
    return null;*/
  }

  // Self may be dead, but we probably have connections to live vertices.
  public Set<Vertex> getAllLiveNonFullVertices() {
    // I love recursion for this kind of thing.
    // But apparently I've gotten up into stack overflow territory.
    // Which I guess makes sense, since I'm working with hundreds or thousands of shapes.
    // So, time to go loopy instead of recursive.

    // Start with a live vertex, any live vertex.  It'll be this one, if this one's alive.
    Vertex startingVertex = findAnyLiveVertex(true);

    Set<Vertex> allVertices = new HashSet<>();
    Set<Vertex> verticesAddedLastRound = new HashSet<>();
    verticesAddedLastRound.add(startingVertex);
    while(verticesAddedLastRound.size() > 0) {
      List<Vertex> verticesToProcess = new ArrayList<>(verticesAddedLastRound);
      verticesAddedLastRound.clear();
      // Owie this is quite the nest.
      for(Vertex vertex : verticesToProcess) {
        for(Shape shape : vertex.wedges) {
          if(shape != null) {
            for(Vertex tempVertex : shape.getVertices()) {
              // Note:  we're doing the dead check here because dead nodes aren't necessary bridges to live nodes,
              // but we're not doing the full check because full nodes might be necessary bridges to other non-full nodes.
              if(!tempVertex.isDead()) {
                if(allVertices.add(tempVertex)) {
                  verticesAddedLastRound.add(tempVertex);
                }
              }
            }
          }
        }
      }
    }

    for(Vertex vertex : allVertices.toArray(new Vertex[]{})) {
      if(vertex.isFull()) {
        allVertices.remove(vertex);
      }
    }
    return allVertices;
  }

}
