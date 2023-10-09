import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class represents a QuadTree object which represents the QuadTree data structure
 */
public class QuadTree {

    /*
     * the amounts of points which can fit inside the QuadTree node
     */
    private final int capacity;
    /*
     * size and location of the QuadTree
     */
    Rectangle boundary;
    /*
     * the points which are in the QuadTree
     */
    private LinkedList<Point> points;
    /*
     * tells if the actual QuadTree is divided or not
     */
    boolean divided = false;
    /*
     * represents the parent tree in which the QuadTree is in
     */
    QuadTree parentTree;

    QuadTree northWest;
    QuadTree northEast;
    QuadTree southWest;
    QuadTree southEast;
    Graph graph;

    /**
     * Constructor method of the QuadTree class
     *
     * @param graph    the given graph
     * @param capacity capacity
     * @param boundary the area the QuadTree node covers
     * @param points   a list of all points which are getting inserted with the insert (Point point) method
     */
    public QuadTree(Graph graph, int capacity, Rectangle boundary, LinkedList<Point> points) {
        this.graph = graph;
        this.boundary = graph.getBoundary();
        this.points = points;
        this.capacity = capacity;
        this.boundary = boundary;
        this.parentTree = this;
    }

    /**
     * This method inserts a Point if the point is inside the boundary
     *
     * @param point the given point
     */
    public void insert(Point point) {
        if (!this.boundary.contains(point)) {
            return;
        }

        if (points.size() < this.capacity) {
            points.push(point);
        } else {
            if (!divided) {
                this.subdivide();
            }
            this.northWest.insert(point);
            this.northEast.insert(point);
            this.southWest.insert(point);
            this.southEast.insert(point);

        }
    }

    /**
     * This method divides the given QuadTree area into four new QuadTree areas (NW, NE, SW, SE), which are inside the boundary,
     * with the size of one-forth of the boundary of the parent QuadTree node
     */
    private void subdivide() {
        double x = boundary.getPoint().getXval();
        double y = boundary.getPoint().getYval();
        double halfEdgeLength = boundary.getHalfEdgeLength();

        Rectangle nw = new Rectangle(new Point(x - halfEdgeLength / 2, y + halfEdgeLength / 2), halfEdgeLength / 2);
        this.northWest = new QuadTree(graph, this.capacity, nw, new LinkedList<>());

        Rectangle ne = new Rectangle(new Point(x + halfEdgeLength / 2, y + halfEdgeLength / 2), halfEdgeLength / 2);
        this.northEast = new QuadTree(graph, this.capacity, ne, new LinkedList<>());

        Rectangle sw = new Rectangle(new Point(x - halfEdgeLength / 2, y - halfEdgeLength / 2), halfEdgeLength / 2);
        this.southWest = new QuadTree(graph, this.capacity, sw, new LinkedList<>());

        Rectangle se = new Rectangle(new Point(x + halfEdgeLength / 2, y - halfEdgeLength / 2), halfEdgeLength / 2);
        this.southEast = new QuadTree(graph, this.capacity, se, new LinkedList<>());


        this.divided = true;
    }

    /**
     * This method calculates the nearest point from the input point
     * <p>
     * (Based on the idea to create a Rectangle with the given point as the center and the halfEdgeLength
     * as the maximum distance from the point to the edge of the parent node, which ensures that there is
     * at least one other node inside the new rectangle. Add all points to a list which are inside this
     * rectangle and calculate the Eucledian distance from searchPoint to all of them and choose the point with
     * the shortest distance.)
     *
     * @param searchPoint input point
     * @return the nearest point
     */
    public Point getNearest(Point searchPoint) {
        Point nearestPoint = null;

        if (graph.getBoundary().contains(searchPoint)) {

            //calculate maxDistance from searchPoint to boundary of parent quadTree
            Rectangle boundary = deepContains(searchPoint, this).getBoundary();
            double maxDistance = maxDistance(searchPoint, boundary);

            //create a rectangle double the size of boundary with point in between
            Rectangle intersectRectangle = new Rectangle(new Point(searchPoint.getXval(), searchPoint.getYval()), maxDistance);
            ArrayList<QuadTree> intersectingList = new ArrayList<>();

            deepIntersects(intersectRectangle, this, intersectingList);

            //calculate distance
            double currentDistance;
            maxDistance = Double.MAX_VALUE;
            for (QuadTree currentTree : intersectingList) {
                for (int j = 0; j < currentTree.getPoints().size(); j++) {
                    Point currentPoint = currentTree.getPoints().get(j);
                    if (intersectRectangle.contains(currentPoint)) {
                        currentDistance = calculateEucledianDistance(searchPoint.getXval(), searchPoint.getYval(), currentPoint.getXval(), currentPoint.getYval());
                        if (currentDistance < maxDistance) {
                            maxDistance = currentDistance;
                            nearestPoint = currentPoint;
                        }
                    }
                }
            }
            return nearestPoint;
        }
        return nearestPoint;
    }


    /**
     * This method does a depth search the second-smallest QuadTree leaf which contains the specific Point
     *
     * @param point the specific point
     * @param tree  the specific QuadTree
     * @return the second-smallest QuadTree which contains the specific Point
     */
    private QuadTree deepContains(Point point, QuadTree tree) {
        //store parent if tree is at min level
        if (!tree.isDivided()) {
            return parentTree = tree.getParent();
        } else {
            //iterate through every quadTree level
            if (tree.isDivided() && tree.getNorthwest().getBoundary().contains(point))
                deepContains(point, tree.getNorthwest());
            if (tree.isDivided() && tree.getNortheast().getBoundary().contains(point))
                deepContains(point, tree.getNortheast());
            if (tree.isDivided() && tree.getSouthwest().getBoundary().contains(point))
                deepContains(point, tree.getSouthwest());
            if (tree.isDivided() && tree.getSoutheast().getBoundary().contains(point))
                deepContains(point, tree.getSoutheast());
        }
        return parentTree;
    }


    /**
     * This method does a depth search to build a  list of QuadTrees which are intersecting with the given
     * rectangle
     *
     * @param rectangle        the given rectangle which intersects with some QuadTrees
     * @param tree             the QuadTrees
     * @param intersectingList a list of intersecting QuadTRees
     */
    private void deepIntersects(Rectangle rectangle, QuadTree tree, ArrayList<QuadTree> intersectingList) {
        //iterate through every QuadTree intersecting with rectangle
        boolean intersected = false;
        if (tree.isDivided()) {
            if (tree.getNortheast().getBoundary().intersects(rectangle)) {
                deepIntersects(rectangle, tree.getNortheast(), intersectingList);
                intersected = true;
            }
            if (tree.getNorthwest().getBoundary().intersects(rectangle)) {
                deepIntersects(rectangle, tree.getNorthwest(), intersectingList);
                intersected = true;
            }
            if (tree.getSoutheast().getBoundary().intersects(rectangle)) {
                deepIntersects(rectangle, tree.getSoutheast(), intersectingList);
                intersected = true;
            }
            if (tree.getSouthwest().getBoundary().intersects(rectangle)) {
                deepIntersects(rectangle, tree.getSouthwest(), intersectingList);
                intersected = true;
            }
        }
        //add quadTree to list if no subdivided quadTree intersected with rectangle
        if (!intersected) intersectingList.add(tree);

    }


    /**
     * This method calculates the maximum distance from the point to the edge of the parent node
     * which should be determined to be the haldEdgeLength of the new rectangle with the searchPoint
     * as centerPoint.
     *
     * @param searchPoint the given searchPoint
     * @param boundary    the parent QuadTree of the smallest QuadTree which contains searchPoint
     * @return the maximum distance from the point to the edge of the parent node
     */
    public double maxDistance(Point searchPoint, Rectangle boundary) {
        double maxDistanceX, maxDistanceY, maxNegDistanceX, maxNegDistanceY;

        //distance to upper left corner
        maxDistanceX = calculateEucledianDistance(searchPoint.getXval(), searchPoint.getYval(),
                boundary.getPoint().getXval() - boundary.getHalfEdgeLength(), boundary.getPoint().getYval() - boundary.getHalfEdgeLength());
        //distance to upper right corner
        maxNegDistanceX = calculateEucledianDistance(searchPoint.getXval(), searchPoint.getYval(),
                boundary.getPoint().getXval() + boundary.getHalfEdgeLength(), boundary.getPoint().getYval() - boundary.getHalfEdgeLength());
        //distance to lower left corner
        maxDistanceY = calculateEucledianDistance(searchPoint.getXval(), searchPoint.getYval(),
                boundary.getPoint().getXval() - boundary.getHalfEdgeLength(), boundary.getPoint().getYval() + boundary.getHalfEdgeLength());
        //distance to lower right corner
        maxNegDistanceY = calculateEucledianDistance(searchPoint.getXval(), searchPoint.getYval(),
                boundary.getPoint().getXval() + boundary.getHalfEdgeLength(), boundary.getPoint().getYval() + boundary.getHalfEdgeLength());

        //Calculate maxDistance to boundary
        maxDistanceX = Math.max(maxDistanceX, maxNegDistanceX);
        maxDistanceY = Math.max(maxDistanceY, maxNegDistanceY);
        return Math.max(maxDistanceX, maxDistanceY);
    }


    /**
     * This method calculates the Eucledian distance of two coordinates
     *
     * @param x1 x-value of first coordinate
     * @param y1 y-value of first coordinate
     * @param x2 x-value of second coordinate
     * @param y2 y-value of second coordinate
     * @return the Eucledian distance
     */
    private double calculateEucledianDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }


    public QuadTree getNortheast() {
        return northEast;
    }

    public QuadTree getNorthwest() {
        return northWest;
    }

    public QuadTree getSoutheast() {
        return southEast;
    }

    public QuadTree getSouthwest() {
        return southWest;
    }

    public boolean isDivided() {
        return divided;
    }

    public LinkedList<Point> getPoints() {
        return points;
    }

    public QuadTree getParent() {
        return this.parentTree;
    }

    public Rectangle getBoundary() {
        return boundary;
    }

}
