package net.origamimarie.penrose.coloring;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.output.SvgOutput;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class ColoredShapeGroup implements Comparable<ColoredShapeGroup> {

  private static String mostOfFileName = "/Users/mariep/personalcode/frame/frame";
  private static int fileNumberMod = 1000000;
  private static AtomicInteger fileNumber = new AtomicInteger(0);
  private ColorPalette colorPalette;
  private ShapeGroup shapeGroup;

  private int cachedEmptyNeighborCount;

  public ColoredShapeGroup(ShapeGroup shapeGroup, ColorPalette colorPalette) {
    this.shapeGroup = shapeGroup;
    shapeGroup.setColoredShapeGroup(this);
    this.colorPalette = colorPalette;
    // For one, this is accurate, for another,
    // the neighbors don't all have shape groups yet so attempts to calculate would NPE.
    cachedEmptyNeighborCount = 0;
  }

  public void recalculateEmptyNeighborCount() {
    int count = 0;
    for(ShapeGroup neighborGroup : shapeGroup.getNeighbors()) {
      count = count + neighborGroup.getColoredShapeGroup().colorPalette.remainingColorCount() == 0 ? 1 : 0;
    }
    cachedEmptyNeighborCount = count;
  }

  public Color getColor() {
    return colorPalette.getCurrentColor();
  }

  @Override
  public int compareTo(ColoredShapeGroup coloredShapeGroup) {
    int comparison = this.colorPalette.compareTo(coloredShapeGroup.colorPalette);
    if(comparison == 0) {
      return Integer.compare(this.cachedEmptyNeighborCount, coloredShapeGroup.cachedEmptyNeighborCount);
      //return Integer.compare(coloredShapeGroup.cachedEmptyNeighborCount, this.cachedEmptyNeighborCount);
    }
    return comparison;
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
                                                         ColoringScheme coloringScheme,
                                                         int maxQueueLoops) {
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

    // We want to know what the first group is so we can do two things.
    // 1.  Pick just one color for it, because if that color fails, we're done.
    // 2.  Catch the problem if it fails, and report correctly.
    ColoredShapeGroup firstGroup = shapeGroupQueue.peek();
    firstGroup.colorPalette.dumpAllButOne();

    List<ColoredShapeGroup> shapesThatHaveBeenColored = new ArrayList<>(shapeGroups.size());
    int queueLoops = 0;
    while(shapeGroupQueue.size() > 0) {
      queueLoops++;
      ColoredShapeGroup tempShapeGroup = shapeGroupQueue.remove();
      Color tempColor = tempShapeGroup.useRandomColor();

      dumpToFile(coloredShapeGroups, coloringScheme);

      ColoringFrame frame = new ColoringFrame(tempColor, tempShapeGroup);
      coloringFrameStack.push(frame);
      shapesThatHaveBeenColored.add(tempShapeGroup);
      for(ShapeGroup neighborGroup : tempShapeGroup.shapeGroup.getNeighbors()) {
        if(!shapesThatHaveBeenColored.contains(neighborGroup.getColoredShapeGroup())) {
          lostColors = neighborGroup.getColoredShapeGroup().informOfNewNeighbor(tempColor);
          // No point recording the removing of color options if none were removed.
          if(lostColors.size() > 0) {
            frame.addGroupAndLostColors(neighborGroup.getColoredShapeGroup(), lostColors);
            // Reset its location in the queue, because it has fewer colors available now.
            reQueueColoredShapeGroups(Collections.singletonList(neighborGroup.getColoredShapeGroup()), shapeGroupQueue);
          }

          ColoringFrame deColoredFrame = null;

          // Woops.  We've treed ourselves.  Time to pop until we have a workable solution.
          if(coloredShapeGroupWillFail(neighborGroup.getColoredShapeGroup())) {
            do {
              // This means we're not on the first go-around, where we didn't want to reset anyway.
              if(deColoredFrame != null) {
                deColoredFrame.groupThatGotColored.colorPalette.resetAttemptedColors();
              }
              deColoredFrame = popStack(coloringFrameStack);
              dumpToFile(coloredShapeGroups, coloringScheme);
              checkFirstGroupAndThrow(deColoredFrame.groupThatGotColored, firstGroup);
              // Recalculate the queue locations of these, since their color counts have changed.
              shapesThatHaveBeenColored.remove(deColoredFrame.groupThatGotColored);
              reQueueColoredShapeGroups(Collections.singletonList(deColoredFrame.groupThatGotColored), shapeGroupQueue);
              reQueueColoredShapeGroups(deColoredFrame.groupsAndTheirLostColors.keySet(), shapeGroupQueue);
            } while(coloredShapeGroupWillFail(deColoredFrame.groupThatGotColored));

            // Don't keep trying to color neighbors, we've undone all of that.
            break;
          }
        }
      }
      dumpToFile(coloredShapeGroups, coloringScheme);
      if(queueLoops > maxQueueLoops) {
        throw new IllegalArgumentException("Sorry!  Your coloring scheme is taking too long, " +
                "you're probably just unlucky this round.");
      }
    }
    log.debug("queueLoops:  {}", queueLoops);

    return coloredShapeGroups;
  }

  // The most obvious thing is that it has no colors available.
  // There may be other possibilities too, haven't figured that out yet.
  private static boolean coloredShapeGroupWillFail(ColoredShapeGroup coloredShapeGroup) {
    return coloredShapeGroup.colorPalette.remainingColorCount() == 0;
  }

  private static void checkFirstGroupAndThrow(ColoredShapeGroup groupThatIsOutOfColors, ColoredShapeGroup firstGroup) {
    if(groupThatIsOutOfColors == firstGroup) {
      throw new IllegalArgumentException("Sorry!  Your coloring scheme could not be completed, " +
              "you probably have too few colors.");
    }
  }

  private static void reQueueColoredShapeGroups(Collection<ColoredShapeGroup> groups,
                                                Queue<ColoredShapeGroup> queue) {
    // Make sure they will have accurate queue location comparisons.
    for(ColoredShapeGroup group : groups) {
      group.recalculateEmptyNeighborCount();
    }
    queue.removeAll(groups);
    queue.addAll(groups);

  }

  private static void dumpToFile(List<ColoredShapeGroup> coloredShapeGroups, ColoringScheme scheme) {
    int currentFileNum = fileNumber.getAndIncrement();
    if(currentFileNum % fileNumberMod == 0) {
      try {
        String previous = mostOfFileName + (currentFileNum-fileNumberMod) + ".html";
        File file = new File(mostOfFileName + currentFileNum + ".html");
        String next = mostOfFileName + (currentFileNum+fileNumberMod) + ".html";
        SvgOutput.shapeGroupsToSvgFile(file, coloredShapeGroups, 40, true, scheme.justTheColors, 0.3, previous, next);
      } catch (Exception ignored) {
        log.debug("Looks like there was an actual exception here");
        ignored.printStackTrace();
      }
    }
  }

  // Gives you a frame that contains
  // - the list of coloredShapesGroups that you should put back in the queue,
  // - the coloredShapeGroup that just got un-colored.
  private static ColoringFrame popStack(Stack<ColoringFrame> frameStack) {
    //log.debug("coloring stack pop");
    ColoringFrame frame = frameStack.pop();
    frame.groupThatGotColored.colorPalette.unsetColor();
    // Give the colors back to these shapeGroups, because using the color in question was a bad idea.
    // I think something is going wrong with the hashing, or something like that.
    // When I do get calls on keys, the values that are clearly in the map at debug time
    // are apparently not getting fetched.
    // So let's do this with Entry objects instead, since we always want the pair anyway.
    for(Map.Entry<ColoredShapeGroup, List<ColorForPalette>> entry : frame.groupsAndTheirLostColors.entrySet()) {
      entry.getKey().addBackColors(entry.getValue());
    }
    return frame;
  }

  // Just a silly little convenience location to store a bunch of coloring schemes.
  public enum ColoringScheme {
    RAINBOW_32_FUZZY_ALTERNATING,
    RAINBOW_32_FUZZY_SIMILAR,
    RAINBOW_6,
    RAINBOW_5,
    CONFETTI;

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
        List<Color> similarColors = copyOutItems(colors32, i-6, 11);
        similarColors.remove(color);
        RAINBOW_32_FUZZY_SIMILAR.colorsForPalette.add(new WhitelistColor(color, similarColors));
      }
      // RAINBOW_6 initialization
      List<Color> colors6 = Arrays.asList(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA);
      RAINBOW_6.colorsForPalette = new ArrayList<>();
      for(Color color : colors6) {
        RAINBOW_6.colorsForPalette.add(new BlacklistColor(color, Collections.singletonList(color)));
      }
      // RAINBOW_5 initialization
      List<Color> colors5 = Arrays.asList(Color.RED, Color.ORANGE, Color.GREEN, Color.BLUE, Color.MAGENTA);
      RAINBOW_5.colorsForPalette = new ArrayList<>();
      for(Color color : colors5) {
        RAINBOW_5.colorsForPalette.add(new BlacklistColor(color, Collections.singletonList(color)));
      }
      // CONFETTI initialization
      CONFETTI.colorsForPalette = new ArrayList<>();
      for(Color color : colors32) {
        CONFETTI.colorsForPalette.add(new WhitelistColor(color, Collections.singletonList(Color.BLACK)));
      }
      CONFETTI.colorsForPalette.add(new BlacklistColor(Color.BLACK, new ArrayList<Color>()));

      for(ColoringScheme scheme : ColoringScheme.values()) {
        scheme.justTheColors = new ArrayList<>();
        for(ColorForPalette paletteColor : scheme.colorsForPalette) {
          scheme.justTheColors.add(paletteColor.color);
        }
      }
    }

    public List<ColorForPalette> colorsForPalette;
    public List<Color> justTheColors;

    private static <T> List<T> copyOutItems(List<T> list, int start, int count) {
      List<T> result = new ArrayList<>();
      for(int i = 0; i < count; i++) {
        if(start < 0) {
          start = list.size() + start;
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
