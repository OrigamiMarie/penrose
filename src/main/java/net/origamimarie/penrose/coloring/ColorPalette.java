package net.origamimarie.penrose.coloring;

import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
public class ColorPalette implements Comparable<ColorPalette> {

  private static Random random = new Random();

  private List<ColorForPalette> colors;
  private List<ColorForPalette> attemptedColors;
  private Color currentColor = null;

  public ColorPalette(List<ColorForPalette> colors) {
    this.colors = colors;
    attemptedColors = new ArrayList<>(colors.size());
  }

  public ColorPalette copy() {
    return new ColorPalette(new ArrayList<>(colors));
  }

  public Color getCurrentColor() {
    return currentColor;
  }

  public List<Color> getUsableColors() {
    List<Color> usableColors = new ArrayList<>(colors.size());
    for(ColorForPalette usableColor : colors) {
      usableColors.add(usableColor.color);
    }
    return usableColors;
  }

  public void dumpAllButOne() {
    while(colors.size() > 1) {
      attemptedColors.add(colors.remove(random.nextInt(colors.size())));
    }
  }

  public Color useRandomColor() {
    if(colors.size() == 0) {
      throw new IllegalArgumentException("There are no available colors to use");
    }
    ColorForPalette tempColorForPalette = colors.remove(random.nextInt(colors.size()));
    attemptedColors.add(tempColorForPalette);
    currentColor = tempColorForPalette.color;
    return currentColor;
  }

  /**
   * Remove from this palette the colors that can't exist next to the new neighbor.
   * If this currently has a color, there will be no effect.
   * @param neighbor New neighbor that just moved in.
   * @return All of the colors that were removed because of the new neighbor.
   */
  public List<ColorForPalette> informOfNewNeighbor(Color neighbor) {
    if(currentColor != null) {
      log.debug("huh, this shouldn't be");
    }
    List<ColorForPalette> removedColors = new ArrayList<>(colors.size());
    if(currentColor == null) {
      for(int i = 0; i < colors.size(); i++) {
        if(!colors.get(i).acceptableNeighbor(neighbor)) {
          removedColors.add(colors.remove(i));
          i--;
        }
      }
    }
    return removedColors;
  }

  public void addBackColors(List<ColorForPalette> colorsForPalette) {
    colors.addAll(colorsForPalette);
  }

  public void unsetColor() {
    this.currentColor = null;
  }

  public void resetAttemptedColors() {
    colors.addAll(attemptedColors);
    attemptedColors.clear();
    currentColor = null;
  }

  public int remainingColorCount() {
    return colors.size();
  }

  @Override
  public int compareTo(ColorPalette cp) {
    return Integer.compare(this.colors.size(), cp.colors.size());
  }

}
