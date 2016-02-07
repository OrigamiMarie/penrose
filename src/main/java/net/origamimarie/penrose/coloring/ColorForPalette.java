package net.origamimarie.penrose.coloring;

import java.awt.Color;

public abstract class ColorForPalette {

  public final Color color;

  public ColorForPalette(Color color) {
    this.color = color;
  }

  public abstract boolean acceptableNeighbor(Color color);

}
