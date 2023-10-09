/**
 * This class represents a Rectangle object with a center point and the width and height from the center to the edge
 * which are getting represented as the half length of the edge
 */
public class Rectangle {

    /*
     * the center point of the rectangle
     */
    private final Point centerPoint;

    /*
     * width and height from the center to the edge
     */
    private final double halfEdgeLength;

    /**
     * the half length of the edge
     *
     * @param point          center point of the rectangle
     * @param halfEdgeLength the width and height of the rectangle from the center to the edge
     */
    public Rectangle(Point point, double halfEdgeLength) {
        this.centerPoint = point;
        this.halfEdgeLength = halfEdgeLength;
    }

    public Point getPoint() {
        return centerPoint;
    }

    public double getHalfEdgeLength() {
        return halfEdgeLength;
    }

    /**
     * This method returns a boolean value which tells if a point is inside a rectangle
     *
     * @param point the given point
     * @return a boolean value which tells if a point is inside a rectangle
     */
    public boolean contains(Point point) {
        return (point.getXval() >= this.centerPoint.getXval() - this.halfEdgeLength) &&
                point.getXval() <= this.centerPoint.getXval() + this.halfEdgeLength &&
                point.getYval() >= this.centerPoint.getYval() - this.halfEdgeLength &&
                point.getYval() <= this.centerPoint.getYval() + this.halfEdgeLength;

    }

    /**
     * This method tells if a rectangle intersects with another rectangle
     *
     * @param range the other rectangle
     * @return a boolean value which tells if the other rectangle intersects with the actual rectangle
     */
    public boolean intersects(Rectangle range) {
        return !(range.getPoint().getXval() - range.getHalfEdgeLength() > this.getPoint().getXval() + this.getHalfEdgeLength() ||
                range.getPoint().getXval() + range.getHalfEdgeLength() < this.getPoint().getXval() - this.getHalfEdgeLength() ||
                range.getPoint().getYval() - range.getHalfEdgeLength() > this.getPoint().getYval() + this.getHalfEdgeLength() ||
                range.getPoint().getYval() + range.getHalfEdgeLength() < this.getPoint().getYval() - this.getHalfEdgeLength());
    }
}