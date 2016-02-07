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

  public static void main(String[] args) throws IOException {
    causeVertexDuplicationError();
  }

  private static void causeVertexDuplicationError() throws IOException {
    for(int i = 0; i < 100; i++) {
      makePrettyThing();
    }
/*
    TilingGenerator generator = new TilingGenerator();
    List<Point> allVertexPoints = new ArrayList<>();
    for(Vertex vertex : generator.getAllVertices()) {
      allVertexPoints.add(vertex.getLocation());
    }
    File file = new File("/Users/mariep/personalcode/penrose/foo.svg");
    SvgOutput.pointListsToSvgFile(file, generator.getAllPointLists(), 50, Color.WHITE, true, allVertexPoints);
    */
  }

  private static void makePrettyThing() throws IOException {
    Point low = new Point(0, 0);
    Point high = new Point(60, 30);
    TilingGenerator generator = new TilingGenerator(low, high);
    /*File file = new File("/Users/mariep/personalcode/penrose/colors.svg");
    Collection<ShapeGroup> shapeGroups = ShapeGroup.generateShapeGroups(generator.getAllVertices(), ShapeGroupType.SINGLE_SHAPES, low, high);
    List<ColoredShapeGroup> coloredShapeGroup = ColoredShapeGroup.colorShapeGroups(shapeGroups, ColoringScheme.RAINBOW_32_FUZZY_SIMILAR);
    log.debug("dumping {} shapeGroups to file", coloredShapeGroup.size());
    SvgOutput.coloredShapeGroupListToSvgFile(file, coloredShapeGroup, 20, true);

    file = new File("/Users/mariep/personalcode/penrose/foo.svg");
    List<Point> allVertexPoints = new ArrayList<>();
    for(Vertex vertex : generator.getAllVertices()) {
      allVertexPoints.add(vertex.getLocation());
    }
    SvgOutput.pointListsToSvgFile(file, generator.getAllPointLists(), 20, Color.WHITE, false, allVertexPoints);
*/
  }
}
