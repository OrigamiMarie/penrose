package net.origamimarie.penrose.generation;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
public class TilingGenerator {

  Vertex liveVertex;

  public TilingGenerator(Point low, Point high) throws IOException {
    liveVertex = new Vertex();
    liveVertex.setLocation(new Point(0.0, 0.0), null);

    boolean finished = false;
    int consecutiveBadAddCount = 0;
    int badAddThreshold = 100;
    while(!finished) {
      boolean badAdd = !liveVertex.addRandomShape();
      consecutiveBadAddCount = badAdd ? consecutiveBadAddCount+1 : 0;
      if(consecutiveBadAddCount >= badAddThreshold) {
        log.debug("{} consecutive bad adds in a row, we're out!", badAddThreshold);
      }
      finished = true;
      for(Vertex tempVertex : liveVertex.getAllLiveNonFullVertices()) {
        if(tempVertex.getLocation().isGreaterThanOrEqual(low) && tempVertex.getLocation().isLessThanOrEqual(high)) {
          liveVertex = tempVertex;
          // Found it.  We can loop again.
          finished = false;
          break;
        }
      }
    }
  }

  public TilingGenerator() {
    liveVertex = new Vertex();
    Vertex originalVertex = liveVertex;
    liveVertex.setLocation(new Point(0.0, 0.0), null);

    Dart currentDart = new Dart();
    liveVertex.addShape(currentDart, Vwedge.D0, 1, true);

    Kite currentKite = (Kite)currentDart.getVertex(Vwedge.G0).getWedges()[1];
    Vertex currentVertex = currentKite.getVertex(Vwedge.K0);
    currentKite = new Kite();
    currentVertex.addShape(currentKite, Vwedge.K0, 9, true);

    currentVertex = currentKite.getVertex(Vwedge.K0);
    currentKite = (Kite)currentVertex.getWedges()[1];
    currentVertex = currentKite.getVertex(Vwedge.M0);
    currentKite = new Kite();
    currentVertex.addShape(currentKite, Vwedge.K0, 2, true);

    currentVertex = currentKite.getVertex(Vwedge.K0);
    currentDart = (Dart)currentVertex.getWedges()[9];
    currentVertex = currentDart.getVertex(Vwedge.E0);
    currentKite = new Kite();
    currentVertex.addShape(currentKite, Vwedge.K0, 0, true);

    currentVertex = currentKite.getVertex(Vwedge.M0);
    currentDart = (Dart)currentVertex.getWedges()[9];
    currentVertex = currentDart.getVertex(Vwedge.G0);
    currentDart = new Dart();
    currentVertex.addShape(currentDart, Vwedge.E0, 1, true);

    currentVertex = currentDart.getVertex(Vwedge.D0);
    currentKite = (Kite)currentVertex.getWedges()[9];
    currentVertex = currentKite.getVertex(Vwedge.K0);
    currentKite = (Kite)currentVertex.getWedges()[8];
    currentVertex = currentKite.getVertex(Vwedge.L0);
    currentDart = new Dart();
    currentVertex.addShape(currentDart, Vwedge.D0, 8, true);

    currentVertex = currentDart.getVertex(Vwedge.E0);
    currentKite = (Kite)currentVertex.getWedges()[9];
    currentVertex = currentKite.getVertex(Vwedge.K0);
    currentKite = (Kite)currentVertex.getWedges()[0];
    currentVertex = currentKite.getVertex(Vwedge.M0);

    currentVertex.addRandomShape();

    /*currentKite = new Kite();
    currentVertex.addShape(currentKite, Vwedge.M0, 8, true);*/
  }

  public List<Point[]> getAllPointLists() {
    return liveVertex.getAllShapePoints();
  }

  public Set<Vertex> getAllVertices() {
    return liveVertex.getAllLiveVertices();
  }
}
