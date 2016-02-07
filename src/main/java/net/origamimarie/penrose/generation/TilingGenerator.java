package net.origamimarie.penrose.generation;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
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
    while(!finished) {
      liveVertex.addRandomShape();
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

    Kite currentKite = new Kite();
    liveVertex.addShape(currentKite, Vwedge.K0, 0, false);

    Dart currentDart = new Dart();
    liveVertex = currentKite.getVertex(Vwedge.N0);
    liveVertex.addShape(currentDart, Vwedge.F5, 2, false);
    Vertex mergeVertex1 = currentDart.getVertex(Vwedge.E0);

    currentKite = new Kite();
    liveVertex = originalVertex;
    liveVertex.addShape(currentKite, Vwedge.K1, 7, false);

    currentDart = new Dart();
    liveVertex = currentKite.getVertex(Vwedge.L0);
    liveVertex.addShape(currentDart, Vwedge.F0, 5, false);
    Vertex mergeVertex2 = currentDart.getVertex(Vwedge.G0);
    log.debug("Here are your vertex points:  {}, {}", mergeVertex1.getLocation(), mergeVertex2.getLocation());
  }

  public List<Point[]> getAllPointLists() {
    return liveVertex.getAllShapePoints();
  }

  public Set<Vertex> getAllVertices() {
    return liveVertex.getAllLiveVertices();
  }
}
