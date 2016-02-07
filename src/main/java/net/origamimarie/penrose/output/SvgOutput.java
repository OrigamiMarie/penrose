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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SvgOutput {

  public static void pointListsToSvgFile(File file, List<Point[]> pointLists, double scaleFactor, Color color, boolean outlines, String ... optionalLinks) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    pointListsToSvg(writer, pointLists, scaleFactor, color, outlines, null, optionalLinks);
    writer.close();
  }

  public static void pointListsToSvgFile(File file, List<Point[]> pointLists, double scaleFactor, Color color, boolean outlines, Collection<Point> vertices, String ... optionalLinks) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    pointListsToSvg(writer, pointLists, scaleFactor, color, outlines, vertices, optionalLinks);
    writer.close();
  }

  public static String pointListsToSvg(List<Point[]> pointLists, double scaleFactor, Color color, boolean outlines) {
    try {
      StringBuilder sb = new StringBuilder();
      pointListsToSvg(sb, pointLists, scaleFactor, color, outlines, null);
      return sb.toString();
    } catch (IOException ignored) {
      // Really, this is a StringBuffer, we're not going to get an IOException.
      return "";
    }
  }

  public static void coloredShapeGroupListToSvgFile(File file, List<ColoredShapeGroup> coloredShapeGroups, double scaleFactor, boolean outlines) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    Map<Point[], Color> pointsWithColors = new HashMap<>();
    for(ColoredShapeGroup coloredShapeGroup : coloredShapeGroups) {
      for(Point[] points : coloredShapeGroup.getShapeGroup().getShapePoints()) {
        pointsWithColors.put(points, coloredShapeGroup.getColor());
      }
    }
    Map<Point[], Color> scaledPointsWithColors = new HashMap<>();
    List<Point[]> scaledPoints = new ArrayList<>();
    for(Point[] points : pointsWithColors.keySet()) {
      Point[] scaled = new Point[points.length];
      scaledPointsWithColors.put(scaled, pointsWithColors.get(points));
      scaledPoints.add(scaled);
      for(int i = 0; i < points.length; i++) {
        if(points[i] != null) {
          scaled[i] = new Point(points[i].x * scaleFactor, points[i].y * -scaleFactor);
        }
      }
    }

    Point[] minAndMax = new Point[2];
    getMinAndMax(scaledPoints, minAndMax);
    Point min = minAndMax[0];
    Point max = minAndMax[1];
    max = max.minus(min);
    Point offset = min.times(-1);

    appendHeader(writer);
    appendSvgHeader(writer, max);

    for(Point[] points : scaledPointsWithColors.keySet()) {
      if(points[0] != null && points[1] != null && points[2] != null && points[3] != null) {
        appendPolygon(writer, points, offset, scaledPointsWithColors.get(points), outlines);
      }
    }
    appendFooter(writer);
    writer.close();
  }

  public static void pointListsToSvg(Appendable ap, List<Point[]> pointLists, double scaleFactor, Color color, boolean outlines, Collection<Point> vertices, String ... links) throws IOException {
    List<Point[]> scaledPoints = new ArrayList<>(pointLists.size());
    for(Point[] points : pointLists) {
      Point[] scaled = new Point[points.length];
      scaledPoints.add(scaled);
      for(int i = 0; i < points.length; i++) {
        if(points[i] != null) {
          scaled[i] = new Point(points[i].x * scaleFactor, points[i].y * -scaleFactor);
        }
      }
    }
    Point[] minAndMax = new Point[2];
    getMinAndMax(scaledPoints, minAndMax);
    Point min = minAndMax[0];
    Point max = minAndMax[1];
    max = max.minus(min);
    Point offset = min.times(-1);

    appendHeader(ap);
    appendLinks(ap, links);
    appendSvgHeader(ap, max);

    float hue = 0.0f;
    for(Point[] points : scaledPoints) {
      if(points[0] != null && points[1] != null && points[2] != null && points[3] != null) {
        appendPolygon(ap, points, offset, (color == null) ? Color.getHSBColor(hue, 1.0f, 1.0f) : color, outlines);
        hue = hue + 0.025f;
      }
    }

    if(vertices != null) {
      for(Point point : vertices) {
        appendVertex(ap, point, offset, scaleFactor, Color.red);
      }
    }

    appendFooter(ap);
  }

  private static void appendLinks(Appendable ap, String ... links) throws IOException {
    if(links != null && links.length > 0) {
      for(String link : links) {
        ap.append("<a href=\"").append(link).append("\">").append(link).append("</a><br>\n");
      }
      ap.append("<hr>");
    }
  }

  private static void getMinAndMax(List<Point[]> pointLists, Point[] minAndMax) {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    for(Point[] pointList : pointLists) {
      for(Point point : pointList) {
        if(point != null) {
          minX = Math.min(minX, point.x);
          minY = Math.min(minY, point.y);
          maxX = Math.max(maxX, point.x);
          maxY = Math.max(maxY, point.y);
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

  private static void appendPolygon(Appendable ap, Point[] points, Point offset, Color color, boolean outlines) throws IOException {
    String colorString = colorToHex(color == null ? Color.WHITE : color);
    ap.append("  <g fill-rule=\"nonzero\" fill=\"#").
            append(colorString);
    ap.append(outlines ? "\" fill-opacity=\"0.3\" stroke=\"black\" stroke-width=\"0.5\" >\n" : "\" fill-opacity=\"0.4\" >\n");
    ap.append("    <path d=\"");
    ap.append("M");
    for(Point point : points) {
      ap.append(" ").append(String.valueOf(point.x + offset.x)).append(",").append(String.valueOf(point.y + offset.y)).append(" L");
    }
    Point point = points[0];
    ap.append(String.valueOf(point.x + offset.x)).append(",").append(String.valueOf(point.y + offset.y)).append(" z ");
    ap.append("\" />\n").append("  </g>\n");
  }

  private static void appendVertex(Appendable ap, Point point, Point offset, double scaleFactor, Color color) throws IOException {
    String colorString = colorToHex(color == null ? Color.WHITE : color);
    ap.append("<circle cx=\"").
            append(String.valueOf(scaleFactor * point.x + offset.x)).
            append("\" cy=\"").
            append(String.valueOf(-scaleFactor * point.y + offset.y)).
            append("\" r=\"").
            append(String.valueOf(scaleFactor/4)).
            append("\" stroke=\"black\" stroke-width=\"0.25\" fill=\"#").
            append(colorString).
            append("\" fill-opacity=\"0.3\" />");
  }

  private static String colorToHex(Color c) {
    return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }

}
