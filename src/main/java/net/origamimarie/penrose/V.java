package net.origamimarie.penrose;

// The sequence starting with D is for Darts, K sequence is for Kites.
public enum V {
  D(0, 0, 2, Dart.class),
  E(1, 1, 1, Dart.class),
  F(2, 2, 6, Dart.class),
  G(3, 3, 1, Dart.class),
  K(4, 0, 2, Kite.class),
  L(5, 1, 2, Kite.class),
  M(6, 2, 4, Kite.class),
  N(7, 3, 2, Kite.class);

  public final int number;
  public final int shapeBasedNumber;
  public final int vertexSize;
  public final Class<? extends Shape> associatedShape;

  V(int number, int shapeBasedNumber, int vertexSize, Class associatedShape) {
    this.number = number;
    this.shapeBasedNumber = shapeBasedNumber;
    this.vertexSize = vertexSize;
    this.associatedShape = associatedShape;
  }

}
