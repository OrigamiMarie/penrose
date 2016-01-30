package net.origamimarie.penrose;

public class Point {
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
}
