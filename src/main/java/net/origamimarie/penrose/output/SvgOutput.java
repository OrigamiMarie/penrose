package net.origamimarie.penrose.output;

import lombok.extern.slf4j.Slf4j;
import net.origamimarie.penrose.coloring.ColoredShapeGroup;
import net.origamimarie.penrose.generation.Point;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SvgOutput {

  public static void shapeGroupsToSvgFile(File file, List<ColoredShapeGroup> coloredShapeGroups,
                                          double scaleFactor, boolean outlines,
                                          List<Color> optionalColorList, double opacity, String... optionalLinks) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));

    List<Point[]> allThePointLists = new ArrayList<>(coloredShapeGroups.size());
    for(ColoredShapeGroup group : coloredShapeGroups) {
      allThePointLists.addAll(group.getShapeGroup().getShapePoints());
    }

    Point[] minAndMax = new Point[2];
    getMinAndMax(allThePointLists, minAndMax, true);
    Point min = minAndMax[0].times(scaleFactor);
    Point max = minAndMax[1].times(scaleFactor);
    max = max.minus(min);
    Point offset = min.times(-1);

    appendHeader(writer);
    appendLinks(writer, optionalLinks);
    appendSvgHeader(writer, max);

    String debuggingClipPathPrefix = "clipPath";
    double debuggingLineWeight = scaleFactor/8;
    List<Point> debuggingColorPointOffsets = new ArrayList<>();
    // Only if we're doing the debugging radiating rainbow thingy.
    if(optionalColorList != null) {
      appendDefsHeader(writer);
      for(int i = 0; i < coloredShapeGroups.size(); i++) {
        ColoredShapeGroup group = coloredShapeGroups.get(i);
        List<Point[]> scaledPointsList = new ArrayList<>();
        for(Point[] points : group.getShapeGroup().getShapePoints()) {
          Point[] scaled = new Point[points.length];
          scaledPointsList.add(scaled);
          for (int j = 0; j < points.length; j++) {
            if (points[j] != null) {
              scaled[j] = new Point(points[j].x * scaleFactor, points[j].y * -scaleFactor);
            }
          }
        }
        appendClipPath(writer, debuggingClipPathPrefix + i, scaledPointsList, offset);
      }
      appendDefsFooter(writer);

      for(int i = 0; i < optionalColorList.size(); i++) {
        debuggingColorPointOffsets.add(new Point
                (Math.cos(2*i*Math.PI/optionalColorList.size())*scaleFactor,
                Math.sin(2*i*Math.PI/optionalColorList.size())*-scaleFactor));
      }
    }

    for(int i = 0; i < coloredShapeGroups.size(); i++) {
      ColoredShapeGroup group = coloredShapeGroups.get(i);
      List<Point[]> pointsList = group.getShapeGroup().getShapePoints();
      for(Point[] points : pointsList) {
        Point[] scaled = new Point[points.length];
        for(int j = 0; j < points.length; j++) {
          if(points[j] != null) {
            scaled[j] = new Point(points[j].x * scaleFactor, points[j].y * -scaleFactor);
          }
        }

        Color outlineColor = Color.black;
        if(optionalLinks != null && optionalLinks.length > 0) {
          if(group.isIslandQueueJumpPriority()) {
            outlineColor = Color.blue;
          }
          if(group.isLastPopQueueJumpPriority()) {
            outlineColor = Color.red;
          }
          if(group.isIslandQueueJumpPriority() && group.isLastPopQueueJumpPriority()) {
            outlineColor = Color.magenta;
          }
        }

        // This means we should draw the options, not the color.
        if(group.getColorPalette().getCurrentColor() == null) {
          appendPolygon(writer, scaled, offset, Color.WHITE, opacity, outlines, outlineColor);
          if(optionalColorList != null) {
            // Get the overall boundaries.
            Point[] localMinAndMax = new Point[2];
            getMinAndMax(Collections.singletonList(scaled), localMinAndMax, false);
            // Find the center.
            Point center = new Point((localMinAndMax[0].x + localMinAndMax[1].x)/2.0,
                    (localMinAndMax[0].y + localMinAndMax[1].y)/2.0);
            center = center.plus(offset);
            for(int j = 0; j < optionalColorList.size(); j++) {
              List<Color> usableColors = group.getColorPalette().getUsableColors();
              if(usableColors.contains(optionalColorList.get(j))) {
                // This means we should fill in this radiating slot.
                appendClippedLine(writer, debuggingClipPathPrefix + i,
                        center, center.plus(debuggingColorPointOffsets.get(j)),
                        debuggingLineWeight, optionalColorList.get(j));
              }
            }
          }

        } else {
          // This means just draw the color.
          appendPolygon(writer, scaled, offset, group.getColor(), opacity, outlines, outlineColor);
        }

      }

    }

    appendFooter(writer);
    writer.close();
  }

  private static void appendLinks(Appendable ap, String ... links) throws IOException {
    if(links != null && links.length > 0) {
      for(String link : links) {
        ap.append("<a href=\"").append(link).append("\">").append(link).append("</a><br>\n");
      }
      ap.append("<hr>");
    }
  }

  private static void getMinAndMax(List<Point[]> pointLists, Point[] minAndMax, boolean flipY) {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    for(Point[] pointList : pointLists) {
      for(Point point : pointList) {
        if(point != null) {
          minX = Math.min(minX, point.x);
          minY = Math.min(minY, flipY ? -point.y : point.y);
          maxX = Math.max(maxX, point.x);
          maxY = Math.max(maxY, flipY ? -point.y : point.y);
        }
      }
    }

    minAndMax[0] = new Point(minX, minY);
    minAndMax[1] = new Point(maxX, maxY);
  }

  private static void appendHeader(Appendable ap) throws IOException {
    ap.append("<html>\n<body>");
  }

  private static void appendSvgHeader(Appendable ap, Point maxes) throws IOException {
    ap.append("<svg height=\"").
            append(String.valueOf(maxes.y)).append("\" width=\"").
            append(String.valueOf(maxes.x)).append("\" version=\"1.1\"\n").
            append("     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
  }

  private static void appendFooter(Appendable ap) throws IOException {
    ap.append("</svg>\n").append("</body>\n</html>\n");
  }

  private static void appendDefsHeader(Appendable ap) throws IOException {
    ap.append("<defs>\n");
  }

  private static void appendDefsFooter(Appendable ap) throws IOException {
    ap.append("</defs>");
  }

  private static void appendClipPath(Appendable ap, String id, List<Point[]> pointsList, Point offset) throws IOException {
    ap.append("<clipPath id=\"").append(id).append("\">");
    for(Point[] points : pointsList) {
      appendPath(ap, points, offset);
    }
    ap.append("</clipPath>");
  }

  private static void appendClippedLine(Appendable ap, String clipId, Point start, Point end,
                                        double lineWidth, Color color) throws IOException {
    ap.append("<line x1=\"").
            append(String.valueOf(start.x)).
            append("\" y1=\"").
            append(String.valueOf(start.y)).
            append("\" x2=\"").
            append(String.valueOf(end.x)).
            append("\" y2=\"").
            append(String.valueOf(end.y)).
            append("\" stroke=\"").
            append(colorToHex(color)).
            append("\" stroke-opacity=\"0.4\" stroke-width=\"").
            append(String.valueOf(lineWidth)).
            append("\" clip-path=\"url(#").
            append(clipId).
            append(")\" />\n");
  }

  private static void appendPolygon(Appendable ap, Point[] points, Point offset,
                                    Color color, double opacity, boolean outlines,
                                    Color outlineColor) throws IOException {
    String colorString = colorToHex(color);
    ap.append("  <g fill-rule=\"nonzero\" fill=\"").
            append(colorString);
    if(outlines) {

      ap.append("\" stroke=\"").
              append(colorToHex(outlineColor)).
              append("\" stroke-width=\"").
              append(outlineColor == Color.black ? "0" : "1").
              append(".5\" ");
    } else {
      ap.append("\" ");
    }
    ap.append("fill-opacity=\"").append(String.valueOf(opacity)).append("\" >\n");
    appendPath(ap, points, offset);
    ap.append("  </g>\n");
  }

  private static void appendPath(Appendable ap, Point[] points, Point offset) throws IOException {
    ap.append("    <path d=\"");
    ap.append("M");
    for(Point point : points) {
      ap.append(" ").append(String.valueOf(point.x + offset.x)).append(",").append(String.valueOf(point.y + offset.y)).append(" L");
    }
    Point point = points[0];
    ap.append(String.valueOf(point.x + offset.x)).append(",").append(String.valueOf(point.y + offset.y)).append(" z ");
    ap.append("\" />\n");
  }

  private static String colorToHex(Color c) {
    if(c == null) {
      return "none";
    }
    return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }

}
