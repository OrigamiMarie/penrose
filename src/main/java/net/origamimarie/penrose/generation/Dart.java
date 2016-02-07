package net.origamimarie.penrose.generation;

import java.util.ArrayList;
import java.util.List;

public class Dart extends Shape {

  public static final V[] DART_VS = new V[]{V.D, V.E, V.F, V.G};
  public static final Vwedge[] DART_VWEDGES = new Vwedge[]{Vwedge.D0, Vwedge.D1, Vwedge.E0, Vwedge.F0, Vwedge.F1, Vwedge.F2, Vwedge.F3, Vwedge.F4, Vwedge.F5, Vwedge.G0};
  // [Vwedge.shapeBaseNumber][orientation where this vwedge is in this vwedge location]
  private static Orientation[][] orientationsByVwedge;

  static {
    List<Orientation> orientations = new ArrayList<>(10);
    // Populate the orientations
    Orientation o = new Orientation();
    o.vWedgeLocations.put(Vwedge.D0, 0);
    o.vWedgeLocations.put(Vwedge.D1, 1);
    o.vWedgeLocations.put(Vwedge.E0, 7);
    o.vWedgeLocations.put(Vwedge.F0, 3);
    o.vWedgeLocations.put(Vwedge.F1, 4);
    o.vWedgeLocations.put(Vwedge.F2, 5);
    o.vWedgeLocations.put(Vwedge.F3, 6);
    o.vWedgeLocations.put(Vwedge.F4, 7);
    o.vWedgeLocations.put(Vwedge.F5, 8);
    o.vWedgeLocations.put(Vwedge.G0, 4);

    Point[] baseVertices = new Point[4];
    baseVertices[V.K.shapeBasedNumber] = new Point(0.0, 0.0);
    baseVertices[V.N.shapeBasedNumber] = new Point(1.0, 0.0);
    baseVertices[V.L.shapeBasedNumber] = new Point(Math.cos(Math.PI*0.4), Math.sin(Math.PI*0.4));
    baseVertices[V.M.shapeBasedNumber] = new Point(0.5, Math.tan(Math.PI*0.2)/2.0);
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
      for(Vwedge vwedge : DART_VWEDGES) {
        orientationsByVwedge[vwedge.shapeBasedNumber][orientation.getWedgeLocation(vwedge)] = orientation;
      }
    }
  }

  protected Orientation getOrientation(Vwedge vwedge, int vwedgeLocation) {
    return orientationsByVwedge[vwedge.shapeBasedNumber][vwedgeLocation];
  }

  protected V[] getVs() {
    return DART_VS;
  }

}
