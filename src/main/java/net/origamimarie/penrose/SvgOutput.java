package net.origamimarie.penrose;

import java.util.ArrayList;
import java.util.List;

public class SvgOutput {

  public static String pointListsToSvg(List<Point[]> pointLists, double scaleFactor) {
    List<Point[]> scaledPoints = new ArrayList<>(pointLists.size());
    for(Point[] points : pointLists) {
      Point[] scaled = new Point[points.length];
      scaledPoints.add(scaled);
      for(int i = 0; i < points.length; i++) {
        scaled[i] = new Point(points[i].x * -scaleFactor, points[i].y * scaleFactor);
      }
    }
    Point[] minAndMax = new Point[2];
    getMinAndMax(scaledPoints, minAndMax);
    Point min = minAndMax[0];
    Point max = minAndMax[1];
    max = max.minus(min);
    Point offset = min.times(-1);

    StringBuilder sb = new StringBuilder();
    appendHeader(sb, max);
    for(Point[] points : scaledPoints) {
      appendPolygon(sb, points, offset);
    }
    appendFooter(sb);
    return sb.toString();
  }

  private static void getMinAndMax(List<Point[]> pointLists, Point[] minAndMax) {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    for(Point[] pointList : pointLists) {
      for(Point point : pointList) {
        minX = Math.min(minX, point.x);
        minY = Math.min(minY, point.y);
        maxX = Math.max(maxX, point.x);
        maxY = Math.max(maxY, point.y);
      }
    }

    minAndMax[0] = new Point(minX, minY);
    minAndMax[1] = new Point(maxX, maxY);
  }

  private static void appendHeader(StringBuilder sb, Point maxes) {
    sb.append("<svg height=\"").
            append((int) maxes.y).append("\" width=\"").
            append((int) maxes.x).append("\" version=\"1.1\"\n").
            append("     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n").
            append("  <g fill-rule=\"nonzero\" fill=\"cyan\" stroke=\"blue\" stroke-width=\"1\" >\n");
  }

  private static void appendFooter(StringBuilder sb) {
    sb.append("  </g>\n").
            append("</svg>\n");
  }

  private static void appendPolygon(StringBuilder sb, Point[] points, Point offset) {
    sb.append("    <path d=\"");
    sb.append("M");
    for(Point point : points) {
      sb.append(" ").append(point.x + offset.x).append(",").append(point.y + offset.y).append(" L");
    }
    Point point = points[0];
    sb.append(point.x + offset.x).append(",").append(point.y + offset.y).append(" z ");
    sb.append("\" />\n");
  }

}
