package net.origamimarie.penrose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Vwedge {

  D0(Dart.class, V.D, 0),
  D1(Dart.class, V.D, 1),
  E0(Dart.class, V.E, 2),
  F0(Dart.class, V.F, 3),
  F1(Dart.class, V.F, 4),
  F2(Dart.class, V.F, 5),
  F3(Dart.class, V.F, 6),
  F4(Dart.class, V.F, 7),
  F5(Dart.class, V.F, 8),
  G0(Dart.class, V.G, 9),
  K0(Kite.class, V.K, 0),
  K1(Kite.class, V.K, 1),
  L0(Kite.class, V.L, 2),
  L1(Kite.class, V.L, 3),
  M0(Kite.class, V.M, 4),
  M1(Kite.class, V.M, 5),
  M2(Kite.class, V.M, 6),
  M3(Kite.class, V.M, 7),
  N0(Kite.class, V.N, 8),
  N1(Kite.class, V.N, 9);

  public static final int WEDGE_COUNT = 10;
  private static Map<V, List<Vwedge>> vsToVwedges;

  static {
    vsToVwedges = new HashMap<>();
    for(V v : V.values()) {
      vsToVwedges.put(v, new ArrayList<Vwedge>());
    }
    for(Vwedge vwedge : Vwedge.values()) {
      vsToVwedges.get(vwedge.v).add(vwedge);
    }
  }

  public final Class<? extends Shape> associatedShape;
  public final V v;
  public final int shapeBasedNumber;

  Vwedge(Class<? extends Shape> associatedShape, V v, int shapeBasedNumber) {
    this.associatedShape = associatedShape;
    this.v = v;
    this.shapeBasedNumber = shapeBasedNumber;
  }

  public static List<Vwedge> getVwedges(V v) {
    return vsToVwedges.get(v);
  }

  public static int normalizeWedgeNumber(int i) {
    // Don't trust any modulus operator interactions with negative numbers.
    while(i < 0) {
      i = i + WEDGE_COUNT;
    }
    return i % WEDGE_COUNT;
  }

}
