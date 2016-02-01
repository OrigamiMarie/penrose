package net.origamimarie.penrose;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SvgOutput {

  public static void pointListsToSvgFile(File file, List<Point[]> pointLists, double scaleFactor, Color color) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    pointListsToSvg(writer, pointLists, scaleFactor, color);
    writer.close();
  }

  public static String pointListsToSvg(List<Point[]> pointLists, double scaleFactor, Color color) {
    try {
      StringBuilder sb = new StringBuilder();
      pointListsToSvg(sb, pointLists, scaleFactor, color);
      return sb.toString();
    } catch (IOException ignored) {
      // Really, this is a StringBuffer, we're not going to get an IOException.
      return "";
    }
  }

  public static void pointListsToSvg(Appendable ap, List<Point[]> pointLists, double scaleFactor, Color color) throws IOException {
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

    appendHeader(ap, max);
    float hue = 0.0f;
    for(Point[] points : scaledPoints) {
      if(points[0] != null && points[1] != null && points[2] != null && points[3] != null) {
        appendPolygon(ap, points, offset, (color == null) ? Color.getHSBColor(hue, 1.0f, 1.0f) : color);
        hue = hue + 0.025f;
      }
    }
    appendFooter(ap);
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

  private static void appendHeader(Appendable ap, Point maxes) throws IOException {
    ap.append("<?xml version=\"1.0\" standalone=\"no\"?>").
            append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">").
            append("<svg height=\"").
            append(String.valueOf(maxes.y)).append("\" width=\"").
            append(String.valueOf(maxes.x)).append("\" version=\"1.1\"\n").
            append("     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
  }

  private static void appendFooter(Appendable ap) throws IOException {
    ap.append("</svg>\n");
  }

  private static void appendPolygon(Appendable ap, Point[] points, Point offset, Color color) throws IOException {
    ap.append("  <g fill-rule=\"nonzero\" fill=\"#").
            append(colorToHex(color)).
            append("\" fill-opacity=\"0.3\" stroke=\"black\" stroke-width=\"0.5\" >\n");
            //append("\" fill-opacity=\"0.3\" >\n");
    ap.append("    <path d=\"");
    ap.append("M");
    for(Point point : points) {
      ap.append(" ").append(String.valueOf(point.x + offset.x)).append(",").append(String.valueOf(point.y + offset.y)).append(" L");
    }
    Point point = points[0];
    ap.append(String.valueOf(point.x + offset.x)).append(",").append(String.valueOf(point.y + offset.y)).append(" z ");
    ap.append("\" />\n").append("  </g>\n");
  }

  private static String colorToHex(Color c) {
    return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
  }

}
