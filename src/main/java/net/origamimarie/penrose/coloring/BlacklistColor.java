package net.origamimarie.penrose.coloring;

import java.awt.Color;
import java.util.List;

public class BlacklistColor extends ColorForPalette {

  private final List<Color> badColors;

  public BlacklistColor(Color color, List<Color> badColors) {
    super(color);
    this.badColors = badColors;
  }

  public boolean acceptableNeighbor(Color neighbor) {
    return !badColors.contains(neighbor);
  }

}
