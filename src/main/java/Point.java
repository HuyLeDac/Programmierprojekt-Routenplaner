/**
 * This class represents a point with a coordinate extracted from the graph clas
 */
public class Point {
    /*
     * x-value (longitude) of the point
     */
    private final double x;

    /*
     * y-value (latitude) of the point
     */
    private final double y;

    /**
     * class constructor of the Point class
     *
     * @param x x-value
     * @param y y-value
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getXval() {
        return x;
    }

    public double getYval() {
        return y;
    }

}