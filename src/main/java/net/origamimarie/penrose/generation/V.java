package net.origamimarie.penrose.generation;

// The sequence starting with D is for Darts, K sequence is for Kites.
public enum V {
  D(0, 0, 2),
  E(1, 1, 1),
  F(2, 2, 6),
  G(3, 3, 1),
  K(4, 0, 2),
  L(5, 1, 2),
  M(6, 2, 4),
  N(7, 3, 2);

  public final int number;
  public final int shapeBasedNumber;
  public final int vertexSize;

  V(int number, int shapeBasedNumber, int vertexSize) {
    this.number = number;
    this.shapeBasedNumber = shapeBasedNumber;
    this.vertexSize = vertexSize;
  }

}
