package net.origamimarie.penrose;

import java.util.HashMap;
import java.util.Map;

public class Orientation {
  public final Map<Vwedge, Integer> vWedgeLocations;
  public final Point[] relativePoints;

  public Orientation() {
    vWedgeLocations = new HashMap<>();
    relativePoints = new Point[4];
  }

  public Orientation rotateOrientationCounterclockwise() {
    Orientation o = new Orientation();
    for(Vwedge vwedge : vWedgeLocations.keySet()) {
      o.vWedgeLocations.put(vwedge, Vwedge.normalizeWedgeNumber(vWedgeLocations.get(vwedge)+1));
    }
    return o;
  }

  public void setRelativePoints(Point[] relativePoints) {
    System.arraycopy(relativePoints, 0, this.relativePoints, 0, 4);
  }

  public Integer getWedgeLocation(Vwedge vwedge) {
    return vWedgeLocations.get(vwedge);
  }
}
