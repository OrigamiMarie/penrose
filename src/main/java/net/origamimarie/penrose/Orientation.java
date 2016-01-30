package net.origamimarie.penrose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Orientation {
  public final Map<V, List<Integer>> vWedgeLocations;
  public final Point[] relativePoints;

  public Orientation() {
    vWedgeLocations = new HashMap<>();
    relativePoints = new Point[4];
  }

  public Orientation rotateOrientationCounterclockwise() {
    Orientation o = new Orientation();
    for(V v : vWedgeLocations.keySet()) {
      List<Integer> list = vWedgeLocations.get(v);
      List<Integer> oList = new ArrayList<>();
      o.vWedgeLocations.put(v, oList);
      for(Integer i : list) {
        oList.add(Vertex.normalizeWedgeNumber(i+1));
      }
    }
    return o;
  }

  public void setRelativePoints(Point[] relativePoints) {
    System.arraycopy(relativePoints, 0, this.relativePoints, 0, 4);
  }

  public List<Integer> getWedgeLocations(V v) {
    return vWedgeLocations.get(v);
  }
}
