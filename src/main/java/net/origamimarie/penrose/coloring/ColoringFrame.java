package net.origamimarie.penrose.coloring;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColoringFrame {

  public Color color;
  public ColoredShapeGroup groupThatGotColored;
  public Map<ColoredShapeGroup, List<ColorForPalette>> groupsAndTheirLostColors;

  public boolean bridgeCreation;
  public List<ColoredShapeGroup> islandShapesThatJumpedTheQueue;


  public ColoringFrame(Color color, ColoredShapeGroup groupThatGotColored) {
    this.color = color;
    this.groupThatGotColored = groupThatGotColored;
    this.groupsAndTheirLostColors = new HashMap<>();
  }

  public void setBridge(List<ColoredShapeGroup> islandShapesThatJumpedTheQueue) {
    bridgeCreation = true;
    this.islandShapesThatJumpedTheQueue = islandShapesThatJumpedTheQueue;
  }

  public void addGroupAndLostColors(ColoredShapeGroup group, List<ColorForPalette> lostColors) {
    groupsAndTheirLostColors.put(group, lostColors);
  }

}
