package net.origamimarie.penrose.generation;

import java.util.ArrayList;
import java.util.List;

public class Kite extends Shape {

  public static final V[] KITE_VS = new V[]{V.K, V.L, V.M, V.N};
  public static final Vwedge[] KITE_VWEDGES = new Vwedge[]{Vwedge.K0, Vwedge.K1, Vwedge.L0, Vwedge.L1, Vwedge.M0, Vwedge.M1, Vwedge.M2, Vwedge.M3, Vwedge.N0, Vwedge.N1};
  // [Vwedge.shapeBaseNumber][orientation where this vwedge is in this vwedge location]
  private static Orientation[][] orientationsByVwedge;

  static {
    List<Orientation> orientations = new ArrayList<>(10);
    // Populate the orientations
    Orientation o = new Orientation();
    o.vWedgeLocations.put(Vwedge.K0, 0);
    o.vWedgeLocations.put(Vwedge.K1, 1);
    o.vWedgeLocations.put(Vwedge.L0, 7);
    o.vWedgeLocations.put(Vwedge.L1, 8);
    o.vWedgeLocations.put(Vwedge.M0, 4);
    o.vWedgeLocations.put(Vwedge.M1, 5);
    o.vWedgeLocations.put(Vwedge.M2, 6);
    o.vWedgeLocations.put(Vwedge.M3, 7);
    o.vWedgeLocations.put(Vwedge.N0, 3);
    o.vWedgeLocations.put(Vwedge.N1, 4);

    Point[] baseVertices = new Point[4];
    baseVertices[V.K.shapeBasedNumber] = new Point(0.0, 0.0);
    baseVertices[V.N.shapeBasedNumber] = new Point(1.0, 0.0);
    baseVertices[V.L.shapeBasedNumber] = new Point(Math.cos(Math.PI*0.4), Math.sin(Math.PI*0.4));
    baseVertices[V.M.shapeBasedNumber] = new Point(Math.cos(Math.PI*0.2), Math.sin(Math.PI*0.2));
    o.setRelativePoints(baseVertices);

    orientations.add(o);
    // The first one is already in the list.
    for(int i = 1; i < Vertex.WEDGE_COUNT; i++) {
      o = o.rotateOrientationCounterclockwise();
      // Why not just let o deal with the rotation?
      // Because we want the most precision we can get,
      // and stacking rotations causes error creep.
      double angle = Math.PI*0.2 * i;
      double cosAngle = Math.cos(angle);
      double sinAngle = Math.sin(angle);
      Point[] rotatedVertices = new Point[4];

      // 0,0 is not going to rotate anywhere.
      rotatedVertices[0] = Point.ORIGIN;
      for(int j = 1; j < 4; j++) {
        Point temp = baseVertices[j];
        rotatedVertices[j] = new Point(temp.x * cosAngle - temp.y * sinAngle,
                temp.x * sinAngle + temp.y * cosAngle);
      }
      o.setRelativePoints(rotatedVertices);
      orientations.add(o);
    }

    orientationsByVwedge = new Orientation[10][Vertex.WEDGE_COUNT];
    for(Orientation orientation : orientations) {
      for(Vwedge vwedge : KITE_VWEDGES) {
        orientationsByVwedge[vwedge.shapeBasedNumber][orientation.getWedgeLocation(vwedge)] = orientation;
      }
    }
  }

  protected Orientation getOrientation(Vwedge vwedge, int vwedgeLocation) {
    return orientationsByVwedge[vwedge.shapeBasedNumber][vwedgeLocation];
  }

  protected V[] getVs() {
    return KITE_VS;
  }

}
