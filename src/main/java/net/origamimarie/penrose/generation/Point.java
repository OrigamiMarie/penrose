package net.origamimarie.penrose.generation;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public class Point {
  private static final double tolerance = 0.000000000000001;

  public static final Point ORIGIN = new Point(0.0, 0.0);

  public final double x;
  public final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Point plus(Point p) {
    return new Point(this.x + p.x, this.y + p.y);
  }

  public Point minus(Point p) {
    return new Point(this.x - p.x, this.y - p.y);
  }

  public Point times(double s) {
    return new Point(this.x * s, this.y * s);
  }

  public boolean isGreaterThanOrEqual(Point p) {
    return this.x >= p.x && this.y >= p.y;
  }

  public boolean isLessThanOrEqual(Point p) {
    return this.x <= p.x && this.y <= p.y;
  }

  public String toString() {
    return "Point{" + x + ", " + y + "}";
  }

  public static class XComparator implements Comparator<Point> {
    @Override
    public int compare(Point p1, Point p2) {
      double diff = p1.x - p2.x;
      if(Math.abs(diff) < tolerance) {
        return 0;
      } else if(diff < 0) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  public static class YComparator implements Comparator<Point> {
    @Override
    public int compare(Point p1, Point p2) {
      double diff = p1.y - p2.y;
      if(Math.abs(diff) < tolerance) {
        return 0;
      } else if(diff < 0) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  // This efficiently keeps a collection of points and lets you know if it
  // contains one that is close enough to what you are looking for.
  public static class PointSet {
    // The Point in the first slot will only be used to compare x values.
    // Sufficiently similar x values will all be stored in the same list.
    // The Points in the list will only be compared by y values
    // (since all in a given list should be equal by x value).
    TreeMap<Point, TreeSet<Point>> xMap;

    public PointSet() {
      xMap = new TreeMap<>(new XComparator());
    }

    public boolean containsSimilar(Point point) {
      TreeSet<Point> ySet = xMap.get(point);
      return ySet != null && ySet.contains(point);
    }

    public Point getSimilarPoint(Point point) {
      TreeSet<Point> ySet = xMap.get(point);
      return ySet == null ? null : (ySet.contains(point) ? ySet.ceiling(point) : null);
    }

    // Returns either the point you just added,
    // or the point that was similar enough that it prevented adding the requested point.
    public void addPoint(Point point) {
      TreeSet<Point> ySet = xMap.get(point);
      if(ySet == null) {
        ySet = new TreeSet<>(new YComparator());
        xMap.put(point, ySet);
      }
      ySet.add(point);
    }
  }

}
