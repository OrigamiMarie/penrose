package net.origamimarie.penrose.coloring;

import java.awt.Color;
import java.util.List;

public class WhitelistColor extends ColorForPalette {

  private final java.util.List<Color> goodColors;

  public WhitelistColor(Color color, List<Color> goodColors) {
    super(color);
    this.goodColors = goodColors;
  }

  public boolean acceptableNeighbor(Color neighbor) {
    return goodColors.contains(neighbor);
  }

}
