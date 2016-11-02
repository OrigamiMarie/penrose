package net.origamimarie.penrose;

import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.coloring.ColoredShapeGroup;
import net.origamimarie.penrose.coloring.ColoringScheme;
import net.origamimarie.penrose.coloring.ShapeGroup;
import static net.origamimarie.penrose.coloring.ShapeGroup.ShapeGroupType;

import net.origamimarie.penrose.generation.Point;
import net.origamimarie.penrose.generation.TilingGenerator;
import net.origamimarie.penrose.output.SvgOutput;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
public class Main {

  private static int xHigh;
  private static int yHigh;

  public static void main(String[] args) throws IOException {
    xHigh = 60;
    yHigh = 60;
    getToException();
  }

  private static void getToException() throws IOException {
    xHigh = 30;
    yHigh = 30;
    for(int i = 0; i < 100; i++) {
      try {
        makePrettyThing();
      } catch (Exception e) {
        log.debug("", e);
        i--;
      }
    }
  }

  private static void makePrettyThing() throws IOException {
    Point low = new Point(0, 0);
    Point high = new Point(xHigh, yHigh);
    TilingGenerator generator = new TilingGenerator(low, high);
    File file = new File("/Users/mariep/personalcode/penrose/colors.svg");
    Collection<ShapeGroup> shapeGroups = ShapeGroup.generateShapeGroups(generator.getAllVertices(),
            ShapeGroupType.SINGLE_SHAPES, ShapeGroup.NeighborsType.VERTICES, low, high);
    if(shapeGroups.size() > 4) {
      try {
        List<ColoredShapeGroup> coloredShapeGroup = ColoredShapeGroup.colorShapeGroups(shapeGroups,
                ColoringScheme.RAINBOW_32_FUZZY_SIMILAR);
        log.debug("dumping {} shapeGroups to file", coloredShapeGroup.size());
        ColoredShapeGroup.resetFileNumber();
        SvgOutput.shapeGroupsToSvgFile(file, coloredShapeGroup, 5, false, null, 0.7);
      } catch (Exception e) {
        ColoredShapeGroup.dumpToFile(true, null);
        log.debug("", e);
      }
    } else {
      throw new IllegalArgumentException("Woops, silly thing, it only made a few shapes!");
    }
  }

}
