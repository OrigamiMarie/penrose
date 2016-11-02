package net.origamimarie.penrose.coloring;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.output.SvgOutput;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public class ColoredShapeGroup implements Comparable<ColoredShapeGroup> {

  private static List<ColoredShapeGroup> currentDebuggingShapeGroups;
  private static ColoringScheme currentDebuggingColoringScheme;


  private static String mostOfFileName = "/Users/mariep/personalcode/frame.noindex/frame";
  private static int fileNumberMod = 1;
  private static int fileNumberThreshold = 500000000;
  private static AtomicInteger fileNumber = new AtomicInteger(0);
  private ColorPalette colorPalette;
  private ShapeGroup shapeGroup;

  private double cachedNullNeighborRatio = 1;
  private boolean islandQueueJumpPriority = false;
  private boolean lastPopQueueJumpPriority = false;

  public ColoredShapeGroup(ShapeGroup shapeGroup, ColorPalette colorPalette) {
    this.shapeGroup = shapeGroup;
    shapeGroup.setColoredShapeGroup(this);
    this.colorPalette = colorPalette;
  }

  public Color getColor() {
    return colorPalette.getCurrentColor();
  }

  public static void resetFileNumber() {
    fileNumber.set(0);
  }

  @Override
  public int compareTo(ColoredShapeGroup csg) {
    if(islandQueueJumpPriority != csg.islandQueueJumpPriority) {
      return islandQueueJumpPriority ? -1 : 1;
    }
    if(lastPopQueueJumpPriority != csg.lastPopQueueJumpPriority) {
      return lastPopQueueJumpPriority ? -1 : 1;
    }
    int paletteComparison = this.colorPalette.compareTo(csg.colorPalette);
    if(paletteComparison != 0) {
      return paletteComparison;
    }
    int cachedNullNeighborComparison = Double.compare(cachedNullNeighborRatio, csg.cachedNullNeighborRatio);
    if(cachedNullNeighborComparison != 0) {
      return cachedNullNeighborComparison;
    }
    return 0;
  }

  private void recalculateNullNeighborRatio() {
    Set<ShapeGroup> neighbors = shapeGroup.getNeighbors();
    cachedNullNeighborRatio = 0;
    for(ShapeGroup neighbor : neighbors) {
      cachedNullNeighborRatio += neighbor.getColoredShapeGroup().getColor() == null ? 1 : 0;
    }
    cachedNullNeighborRatio /= neighbors.size();
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
    currentDebuggingColoringScheme = coloringScheme;
    currentDebuggingShapeGroups = coloredShapeGroups;

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
    while(shapeGroupQueue.size() > 0) {
      ColoredShapeGroup tempShapeGroup = shapeGroupQueue.remove();
      // Now that it's being colored, it doesn't need priority.
      tempShapeGroup.lastPopQueueJumpPriority = false;
      Color tempColor = tempShapeGroup.useRandomColor();

      dumpToFile(false, Collections.singletonList(tempShapeGroup));

      Set<ShapeGroup> island = tempShapeGroup.shapeGroup.getNeighboringIsland();

      ColoringFrame frame = new ColoringFrame(tempColor, tempShapeGroup);

      // Looks like there's an island nearby.
      // We should prioritize filling the island.
      if(island != null) {
        List<ColoredShapeGroup> islandPiecesForFrame = new ArrayList<>(island.size());
        for(ShapeGroup islandGroup : island) {
          // No need to requeue it if it's already high priority.
          if(!islandGroup.getColoredShapeGroup().islandQueueJumpPriority) {
            islandPiecesForFrame.add(islandGroup.getColoredShapeGroup());
            shapeGroupQueue.remove(islandGroup.getColoredShapeGroup());
            islandGroup.getColoredShapeGroup().islandQueueJumpPriority = true;
            if(islandGroup.getColoredShapeGroup().getColor() != null) {
              log.debug("Hey, this island shape has color in it!");
            }
            shapeGroupQueue.add(islandGroup.getColoredShapeGroup());
          }
        }
        frame.setBridge(islandPiecesForFrame);
      }


      coloringFrameStack.push(frame);
      shapesThatHaveBeenColored.add(tempShapeGroup);
      for(ShapeGroup neighborGroup : tempShapeGroup.shapeGroup.getNeighbors()) {
        if(!shapesThatHaveBeenColored.contains(neighborGroup.getColoredShapeGroup())) {
          lostColors = neighborGroup.getColoredShapeGroup().informOfNewNeighbor(tempColor);

          // Remove from queue, mess with its priority, put it back in.
          shapeGroupQueue.remove(neighborGroup.getColoredShapeGroup());
          // No point recording the removing of color options if none were removed.
          if(lostColors.size() > 0) {
            frame.addGroupAndLostColors(neighborGroup.getColoredShapeGroup(), lostColors);
          }
          neighborGroup.getColoredShapeGroup().recalculateNullNeighborRatio();
          if(neighborGroup.getColoredShapeGroup().getColor() != null) {
            log.debug("Hey, this neighborGroup has color in it!");
          }
          shapeGroupQueue.add(neighborGroup.getColoredShapeGroup());


          ColoringFrame deColoredFrame = null;
          // Woops.  We've treed ourselves.  Time to pop until we have a workable solution.
          if(coloredShapeGroupWillFail(neighborGroup.getColoredShapeGroup())) {
            do {
              if(deColoredFrame != null) {
                // This means we're not on the first go-around, so we should reset colors.
                deColoredFrame.groupThatGotColored.colorPalette.resetAttemptedColors();
              }
              // If deColoredFrame is null, we're popping the last in a series of bad choices.
              // It's likely that we should prioritize this item higher so that it can get tried
              // earlier next round.
              deColoredFrame = popStackOntoQueue(coloringFrameStack, shapeGroupQueue,
                      deColoredFrame == null);
              dumpToFile(false, null);
              checkFirstGroupAndThrow(deColoredFrame.groupThatGotColored, firstGroup);
              shapesThatHaveBeenColored.remove(deColoredFrame.groupThatGotColored);
            } while(coloredShapeGroupWillFail(deColoredFrame.groupThatGotColored));

            // Don't keep trying to color neighbors, we've undone all of that.
            break;
          }
        }
      }
      dumpToFile(false, null);
    }

    return coloredShapeGroups;
  }

  private static boolean coloredShapeGroupWillFail(ColoredShapeGroup coloredShapeGroup) {
    return coloredShapeGroup.colorPalette.remainingColorCount() == 0;
  }

  private static void checkFirstGroupAndThrow(ColoredShapeGroup groupThatIsOutOfColors, ColoredShapeGroup firstGroup) {
    if(groupThatIsOutOfColors == firstGroup) {
      throw new IllegalArgumentException("Sorry!  Your coloring scheme could not be completed");
    }
  }

  public static void dumpToFile(boolean override, Collection<ColoredShapeGroup> extraGroups) {
    int currentFileNum = fileNumber.getAndIncrement();
    if(currentFileNum > fileNumberThreshold) {
      fileNumberMod = 1;
    }
    if(currentFileNum % fileNumberMod == 0 || override) {
      try {
        String previous = mostOfFileName + (currentFileNum-fileNumberMod) + ".html";
        File file = new File(mostOfFileName + currentFileNum + ".html");
        String next = mostOfFileName + (currentFileNum+fileNumberMod) + ".html";
        List<ColoredShapeGroup> groupsToDump = currentDebuggingShapeGroups;
        if(extraGroups != null) {
          groupsToDump = new ArrayList<>(currentDebuggingShapeGroups);
          groupsToDump.addAll(extraGroups);
        }
        SvgOutput.shapeGroupsToSvgFile(file, groupsToDump, 30, true, currentDebuggingColoringScheme.justTheColors, 0.3, previous, next);
      } catch (Exception ignored) {
        log.debug("Looks like there was an actual exception here");
        ignored.printStackTrace();
      }
    }
  }

  // This does the queue manipulation in addition to the stack manipulation,
  // mostly because the ColoredShapeGroups need to not be in the queue while they are
  // having their color counts changed (because heap-based priority queues act weird
  // when items in the queue have their priority levels change).
  private static ColoringFrame popStackOntoQueue(Stack<ColoringFrame> frameStack,
                                                 Queue<ColoredShapeGroup> queue,
                                                 boolean lastPopQueueJumpPriority) {
    ColoringFrame frame = frameStack.pop();
    frame.groupThatGotColored.colorPalette.unsetColor();
    frame.groupThatGotColored.setLastPopQueueJumpPriority(lastPopQueueJumpPriority);
    queue.add(frame.groupThatGotColored);
    // Give the colors back to these shapeGroups, because using the color in question was a bad idea.
    // I think something is going wrong with the hashing, or something like that.
    // When I do get calls on keys, the values that are clearly in the map at debug time
    // are apparently not getting fetched.
    // So let's do this with Entry objects instead, since we always want the pair anyway.
    for(Map.Entry<ColoredShapeGroup, List<ColorForPalette>> entry : frame.groupsAndTheirLostColors.entrySet()) {
      ColoredShapeGroup group = entry.getKey();
      queue.remove(group);
      group.addBackColors(entry.getValue());
      // This group's neighbor just lost its color, so this number will change.
      group.recalculateNullNeighborRatio();
      if(group.getColor() != null) {
        log.debug("Frame neighbor has color in it!");
      }
      queue.add(group);
    }
    if(frame.bridgeCreation) {
      for(ColoredShapeGroup group : frame.islandShapesThatJumpedTheQueue) {
        queue.remove(group);
        group.setIslandQueueJumpPriority(false);
        queue.add(group);
      }
    }
    return frame;
  }

}
