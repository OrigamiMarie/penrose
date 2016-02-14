package net.origamimarie.penrose;

import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.coloring.ColoredShapeGroup;
import static net.origamimarie.penrose.coloring.ColoredShapeGroup.ColoringScheme;
import net.origamimarie.penrose.coloring.ShapeGroup;
import static net.origamimarie.penrose.coloring.ShapeGroup.ShapeGroupType;
import net.origamimarie.penrose.generation.Point;
import net.origamimarie.penrose.generation.TilingGenerator;
import net.origamimarie.penrose.generation.Vertex;
import net.origamimarie.penrose.output.SvgOutput;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class Main {

  private static int xHigh;
  private static int yHigh;

  public static void main(String[] args) throws IOException {
    getToException();
  }

  private static void getToDebugPoint() throws IOException {
    xHigh = 60;
    yHigh = 30;
    for(int a = 0; a < 6; a++) {
      boolean success = false;
      for(int i = 0; i < 30 && !success; i++) {
        log.debug("{}", i);
        try {
          makePrettyThing();
          success = true;
        } catch (IllegalArgumentException | NullPointerException ignored) {}
      }
      xHigh = xHigh + 10;
      yHigh = yHigh + 10;
    }
  }

  private static void getToException() throws IOException {
    for(int i = 0; i < 10000; i++) {
      Point low = new Point(0, 0);
      Point high = new Point(30, 15);
      TilingGenerator generator = new TilingGenerator(low, high);
      log.debug("created {}", i);
    }
  }

  private static void makePrettyThing() throws IOException {
    Point low = new Point(0, 0);
    Point high = new Point(xHigh, yHigh);
    TilingGenerator generator = new TilingGenerator(low, high);
    File file = new File("/Users/mariep/personalcode/penrose/colors.svg");
    Collection<ShapeGroup> shapeGroups = ShapeGroup.generateShapeGroups(generator.getAllVertices(), ShapeGroupType.SINGLE_SHAPES, low, high);
    if(shapeGroups.size() > 4) {
      List<ColoredShapeGroup> coloredShapeGroup = ColoredShapeGroup.colorShapeGroups(shapeGroups, ColoringScheme.RAINBOW_32_FUZZY_SIMILAR, shapeGroups.size()*4);
      log.debug("dumping {} shapeGroups to file", coloredShapeGroup.size());
      SvgOutput.shapeGroupsToSvgFile(file, coloredShapeGroup, 10, false, null, 0.4);
    } else {
      throw new IllegalArgumentException("Woops, silly thing, it only made a few shapes!");
    }
  }
}
