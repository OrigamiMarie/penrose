package net.origamimarie.penrose.coloring;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

@Data
@Slf4j
public class ColoredShapeGroup implements Comparable<ColoredShapeGroup> {

  private ColorPalette colorPalette;
  private ShapeGroup shapeGroup;

  public ColoredShapeGroup(ShapeGroup shapeGroup, ColorPalette colorPalette) {
    this.shapeGroup = shapeGroup;
    shapeGroup.setColoredShapeGroup(this);
    this.colorPalette = colorPalette;
  }

  public Color getColor() {
    return colorPalette.getCurrentColor();
  }

  @Override
  public int compareTo(ColoredShapeGroup csg) {
    return this.colorPalette.compareTo(csg.colorPalette);
  }

  public Color useRandomColor() {
    return colorPalette.useRandomColor();
  }

  public List<ColorForPalette> informOfNewNeighbor(Color color) {
    return this.colorPalette.informOfNewNeighbor(color);
  }

  public void addBackColors(List<ColorForPalette> colorsForPalette) {
    colorPalette.addBackColors(colorsForPalette);
  }

  public static List<ColoredShapeGroup> colorShapeGroups(Collection<ShapeGroup> shapeGroups,
                                                         ColoringScheme coloringScheme) {
    ColorPalette originalPalette = new ColorPalette(coloringScheme.colorsForPalette);
    List<ColoredShapeGroup> coloredShapeGroups = new ArrayList<>(shapeGroups.size());
    // Prime all of the coloredShapeGroups
    for(ShapeGroup tempShapeGroup : shapeGroups) {
      coloredShapeGroups.add(new ColoredShapeGroup(tempShapeGroup, originalPalette.copy()));
    }

    Queue<ColoredShapeGroup> shapeGroupQueue = new PriorityQueue<>(shapeGroups.size());
    shapeGroupQueue.addAll(coloredShapeGroups);

    Stack<ColoringFrame> coloringFrameStack = new Stack<>();
    List<ColorForPalette> lostColors;

    List<ColoredShapeGroup> shapesThatHaveBeenColored = new ArrayList<>(shapeGroups.size());
    while(shapeGroupQueue.size() > 0) {
      ColoredShapeGroup tempShapeGroup = shapeGroupQueue.remove();
      Color tempColor = tempShapeGroup.useRandomColor();
      ColoringFrame frame = new ColoringFrame(tempColor, tempShapeGroup);
      coloringFrameStack.push(frame);
      shapesThatHaveBeenColored.add(tempShapeGroup);
      for(ShapeGroup neighborGroup : tempShapeGroup.shapeGroup.getNeighbors()) {
        if(!shapesThatHaveBeenColored.contains(neighborGroup.getColoredShapeGroup())) {
          lostColors = neighborGroup.getColoredShapeGroup().informOfNewNeighbor(tempColor);
          frame.addGroupAndLostColors(neighborGroup.getColoredShapeGroup(), lostColors);
          // Reset its location in the queue, because it has fewer colors available now.
          shapeGroupQueue.remove(neighborGroup.getColoredShapeGroup());
          shapeGroupQueue.add(neighborGroup.getColoredShapeGroup());

          // Woops.  We've treed ourselves.  Time to pop until we have a workable solution.
          if(neighborGroup.getColoredShapeGroup().colorPalette.remainingColorCount() == 0) {
            ColoringFrame deColoredFrame = popStack(coloringFrameStack);
            // Recalculate the queue locations of these, since their color counts have changed.

            shapesThatHaveBeenColored.removeAll(deColoredFrame.groupsAndTheirLostColors.keySet());
            shapeGroupQueue.removeAll(deColoredFrame.groupsAndTheirLostColors.keySet());
            shapeGroupQueue.addAll(deColoredFrame.groupsAndTheirLostColors.keySet());
            // This means that was the last color choice for groupThatGotColored.
            // It's out of choices, so we need to reinstate it and pop the next frame.
            // This is slightly different from what we just did above, and could potentially loop.
            while(deColoredFrame.groupThatGotColored.colorPalette.remainingColorCount() == 0) {
              log.debug("tried all the colors for this shape");
              deColoredFrame.groupThatGotColored.colorPalette.resetAttemptedColors();
              deColoredFrame = popStack(coloringFrameStack);
              // Recalculate the queue locations of these, since their color counts have changed.
              shapesThatHaveBeenColored.removeAll(deColoredFrame.groupsAndTheirLostColors.keySet());
              shapeGroupQueue.removeAll(deColoredFrame.groupsAndTheirLostColors.keySet());
              shapeGroupQueue.addAll(deColoredFrame.groupsAndTheirLostColors.keySet());
            }
            // Don't keep trying to color neighbors, we've undone all of that.
            break;
          }
        }
      }
    }

    return coloredShapeGroups;
  }

  // Gives you a frame that contains
  // - the list of coloredShapesGroups that you should put back in the queue,
  // - the coloredShapeGroup that just got un-colored.
  private static ColoringFrame popStack(Stack<ColoringFrame> frameStack) {
    log.debug("coloring stack pop");
    ColoringFrame frame = frameStack.pop();
    // Give the colors back to these shapeGroups, because using the color in question was a bad idea.
    for(ColoredShapeGroup colorsLostGroup : frame.groupsAndTheirLostColors.keySet()) {
      List<ColorForPalette> colorsForPalette = frame.groupsAndTheirLostColors.get(colorsLostGroup);
      colorsLostGroup.addBackColors(colorsForPalette);
    }
    return frame;
  }

  // Just a silly little convenience location to store a bunch of coloring schemes.
  public enum ColoringScheme {
    RAINBOW_32_FUZZY_ALTERNATING,
    RAINBOW_32_FUZZY_SIMILAR,
    RAINBOW_6;

    static {
      List<Color> colors32 = new ArrayList<>(32);
      for(int i = 0; i < 32; i++) {
        colors32.add(Color.getHSBColor((i * 8.0f) / 256.0f, 1, 1));
      }
      // RAINBOW_32_FUZZY_ALTERNATING initialization
      RAINBOW_32_FUZZY_ALTERNATING.colorsForPalette = new ArrayList<>();
      for(int i = 0; i < colors32.size(); i++) {
        Color color = colors32.get(i);
        List<Color> dissimilarColors = copyOutItems(colors32, i+4, 23);
        RAINBOW_32_FUZZY_ALTERNATING.colorsForPalette.add(new WhitelistColor(color, dissimilarColors));
      }
      // RAINBOW_32_FUZZY_SIMILAR initialization
      RAINBOW_32_FUZZY_SIMILAR.colorsForPalette = new ArrayList<>();
      for(int i = 0; i < colors32.size(); i++) {
        Color color = colors32.get(i);
        List<Color> similarColors = copyOutItems(colors32, i-3, 7);
        similarColors.remove(color);
        RAINBOW_32_FUZZY_SIMILAR.colorsForPalette.add(new WhitelistColor(color, similarColors));
      }
      // RAINBOW_6 initialization
      RAINBOW_6.colorsForPalette = new ArrayList<>();

    }

    public List<ColorForPalette> colorsForPalette;

    private static <T> List<T> copyOutItems(List<T> list, int start, int count) {
      List<T> result = new ArrayList<>();
      for(int i = 0; i < count; i++) {
        if(start < 0) {
          start = list.size() - start;
        }
        if(start > list.size() - 1) {
          start = start - (list.size() - 1);
        }
        result.add(list.get(start));
        start++;
      }
      return result;
    }

  }

}
