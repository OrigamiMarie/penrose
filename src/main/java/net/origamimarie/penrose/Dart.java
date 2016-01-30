package net.origamimarie.penrose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Dart extends Shape {

  public static final V[] DART_VS = new V[]{V.D, V.E, V.F, V.G};
  // [V.shapeBasedNumber][orientation where this number is the most clockwise part of that V]
  // That's sort of confusing.  Sorry.
  private static Orientation[][] orientationsByClockwiseV;

  static {
    List<Orientation> orientations = new ArrayList<>(10);
    // Populate the orientations
    Orientation o = new Orientation();
    o.vWedgeLocations.put(V.D, Arrays.asList(0, 1));
    o.vWedgeLocations.put(V.E, Collections.singletonList(7));
    o.vWedgeLocations.put(V.F, Arrays.asList(3, 4, 5, 6, 7, 8));
    o.vWedgeLocations.put(V.G, Collections.singletonList(4));

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

    orientationsByClockwiseV = new Orientation[4][Vertex.WEDGE_COUNT];
    for(Orientation orientation : orientations) {
      for(V v : DART_VS) {
        // Because nothing gets shuffled during a rotation and all lists started with the most clockwise,
        // these rotated orientations will start with the most clockwise too.
        orientationsByClockwiseV[v.shapeBasedNumber][orientation.getWedgeLocations(v).get(0)] = orientation;
      }
    }
  }

  protected Orientation getOrientation(V v, int clockwiseMostWedge) {
    return orientationsByClockwiseV[v.shapeBasedNumber][clockwiseMostWedge];
  }

  protected V[] getVs() {
    return DART_VS;
  }

}
