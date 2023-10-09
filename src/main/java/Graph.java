import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


/**
 * Represents a graph of the specific file.
 * IMPORTANT: Only works with the .fmi files from this website:
 * https://fmi.uni-stuttgart.de/alg/research/stuff/
 */
public class Graph {

    /*
     * Name of Data file
     */
    private final File file;
    /*
     * A multidimensional array which stores all edges
     */
    private int[][] adjacencyList;
    /*
     * An array which stores the offsets of all nodes
     */
    private int[] offsetList;
    /*
     * A multidimensional array which stores the latitude and longitude
     * of each node
     */
    private double[][] locationList;
    /*
     * An object which can use path solving algorithms
     */
    private final PathFinder pathFinder;

    private final double maxX;
    private final double maxY;
    private final double minX;
    private final double minY;

    QuadTree qtree;


    /**
     * Class constructor for the Graph
     *
     * @param file file from the given Website
     * @throws FileNotFoundException in case the file doesn't exist
     */
    public Graph(File file) throws FileNotFoundException {
        this.file = file;
        this.buildLists();
        this.pathFinder = new PathFinder(this);
        this.maxX = getMaxX();
        this.maxY = getMaxY();
        this.minX = getMinX();
        this.minY = getMinY();
        this.qtree = this.buildQuadTree(3);

    }

    /**
     * This Method builds all necessary Data Structures to design the route planer.
     */
    private void buildLists() {
        try {
            Scanner scanner = new Scanner(file);

            //Skip first 5 lines
            for (int i = 0; i < 5; i++) {
                scanner.nextLine();
            }

            //Node count and edge count
            int nodeCount = Integer.parseInt(scanner.nextLine());
            int edgeCount = Integer.parseInt(scanner.nextLine());

            //Line to split lines into single values
            String[] currentLine;

            //Edge values
            int srcIDX;
            int trgIDX;
            int weight;
            int currentNode = 0;
            int offset = 0;


            //Create locationList
            locationList = new double[nodeCount][2];
            //Create offsetList + setting first Index to 0
            offsetList = new int[nodeCount + 1];
            //Create adjacencecyMatrix
            adjacencyList = new int[edgeCount][3];

            /*
             * build locationList
             */
            for (int i = 0; i < nodeCount; i++) {
                currentLine = scanner.nextLine().split(" ");
                locationList[i][0] = Double.parseDouble(currentLine[2]);
                locationList[i][1] = Double.parseDouble(currentLine[3]);
            }

            /*
             * build offsetList & adjacencyMatrix
             */
            for (int i = 0; i < edgeCount; i++) {
                //Split lines into values
                currentLine = scanner.nextLine().split(" ");
                srcIDX = Integer.parseInt(currentLine[0]);
                trgIDX = Integer.parseInt(currentLine[1]);
                weight = Integer.parseInt(currentLine[2]);

                //source
                adjacencyList[i][0] = srcIDX;
                //target
                adjacencyList[i][1] = trgIDX;
                //cost of the edge
                adjacencyList[i][2] = weight;


                //Update Offset to all other Nodes
                offsetList[0] = 0;
                if (currentNode != srcIDX) {
                    for (int j = currentNode; j < srcIDX; j++) {
                        offsetList[j + 1] = offset;
                    }
                    currentNode = srcIDX;
                }

                //Set offset changes
                offset++;
                offsetList[srcIDX + 1] = offset;
            }

            scanner.close();


        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }
    }

    /**
     * This method returns the source index of the given Edge
     *
     * @param edgeIDX edge index
     * @return source index of edgeIDX
     */
    public int getSrcIDXofEdge(int edgeIDX) {
        return adjacencyList[edgeIDX][0];
    }

    /**
     * This method returns the target index of the given Edge
     *
     * @param edgeIDX edge index
     * @return target index of edgeIDX
     */
    public Integer getTrgIDXofEdge(int edgeIDX) {
        return adjacencyList[edgeIDX][1];
    }

    /**
     * This method returns the weight of the given Edge
     *
     * @param edgeIDX edge index
     * @return weight of edgeIDX
     */
    public Integer getWeightOfEdge(int edgeIDX) {
        return adjacencyList[edgeIDX][2];
    }

    /**
     * This method returns the offset of the requested node index from the offsetList
     *
     * @param nodeIDX node Index
     * @return offset of the given node
     */
    public Integer getOffsetOfNode(int nodeIDX) {
        return offsetList[nodeIDX];
    }

    /**
     * This method returns the latitude of the node
     *
     * @param nodeIDX
     * @return a double value which represents the latitude
     */
    public Double getLatitudeOfNode(int nodeIDX) {
        return locationList[nodeIDX][0];
    }

    /**
     * This method returns the longitude of the node
     *
     * @param nodeIDX
     * @return a double value which represents the longitude
     */
    public Double getLongitudeOfNode(int nodeIDX) {
        return locationList[nodeIDX][1];
    }

    /**
     * This method returns the number of nodes
     *
     * @return number of nodes
     */
    public int getNumberOfNodes() {
        return locationList.length;
    }

    /**
     * This method returns the value of the shortest path from the starting point
     * to the end point using the One to One Dijkstra algorithm.
     *
     * @param startingPoint the source node
     * @param endPoint      the targetNode
     * @return the value of the shortest path
     */
    public int findRouteAtoB(int startingPoint, int endPoint) {
        return pathFinder.oneToOneDijkstra(startingPoint, endPoint);
    }

    /**
     * This method solves the shortest path from the starting index to all node index
     *
     * @param startingPoint
     * @return int
     */
    public int[] findRouteAtoAll(int startingPoint) {
        return pathFinder.oneToAllDijkstra(startingPoint);
    }

    /**
     * this method returns a list of coordinates in a specific order which represents the 
     * path from the startingPoint to the endPoint.
     * 
     * @param startingPoint
     * @param endPoint
     * @return
     */
    public List<String> getPathCoords(int startingPoint, int endPoint){
    	return pathFinder.pathFromAToBCoords(startingPoint, endPoint);
    }

    /**
     * This method returns an rectangle-shaped area, where all nodes
     * with their given coordinates could fit in
     *
     * @return an rectangle-shaped area
     */
    public Rectangle getBoundary() {

        double width = maxX - minX;
        double height = maxY - minY;
        final double dim = Math.max(width, height);

        return new Rectangle(new Point((minX + (dim / 2)), (minY + (dim / 2))), dim / 2);
    }


    /**
     * This method builds a new QuadTree data structure using the locationList array
     *
     * @param capacity the maximum amount of points (or nodes) that could fit in a
     *                 QuadTree leaf
     * @return the new QuadTree
     */
    public QuadTree buildQuadTree(int capacity) {
        this.qtree = new QuadTree(this, capacity, this.getBoundary(), new LinkedList<>());
        for (int i = 0; i < locationList.length; i++) {
            qtree.insert(new Point(this.getLongitudeOfNode(i), this.getLatitudeOfNode(i)));
        }
        return qtree;
    }

    /**
     * This method calculates the nearest node from the given longitude and latitude.
     *
     * @param lon Longitude
     * @param lat Latitude
     * @return a Point with the
     * @throws IllegalStateException
     */
    public double[] getClosestDistance(double lon, double lat) throws IllegalStateException {
        double[] coords = {0.0, 0.0};

        if (this.qtree == null) {
            throw new IllegalStateException("Quadtree is not built.");
        }
        Point closestPoint = qtree.getNearest(new Point(lon, lat));
        coords[1] = closestPoint.getYval();
        coords[0] = closestPoint.getXval();

        return coords;
    }

    /**
     * This method calculates the x value most right point
     *
     * @return the x value of the most right point
     */
    private double getMaxX() {

        double maxX = Double.MIN_VALUE;

        for (int i = 0; i < locationList.length; i++) {
            if (maxX < this.getLongitudeOfNode(i)) {
                maxX = this.getLongitudeOfNode(i);
            }
        }

        return maxX;

    }

    /**
     * This method calculates the x value most left point
     *
     * @return @return the x value of the most left point
     */
    private double getMinX() {

        double minX = Double.MAX_VALUE;

        for (int i = 0; i < locationList.length; i++) {
            if (minX > this.getLongitudeOfNode(i)) {
                minX = this.getLongitudeOfNode(i);
            }
        }

        return minX;

    }

    /**
     * This method calculates the y value highest point
     *
     * @return the y value of the highest point
     */
    private double getMaxY() {

        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < locationList.length; i++) {
            if (maxY < this.getLatitudeOfNode(i)) {
                maxY = this.getLatitudeOfNode(i);
            }
        }

        return maxY;
    }

    /**
     * This method calculates the y value lowest point
     *
     * @return the y value of the lowest point
     */
    private double getMinY() {

        double minY = Double.MAX_VALUE;

        for (int i = 0; i < locationList.length; i++) {
            if (minY > this.getLatitudeOfNode(i)) {
                minY = this.getLatitudeOfNode(i);
            }
        }

        return minY;

    }
    
    /**
     * This method returns the index of the node by giving a specific coordinate.
     * 
     * @param lonlat coordinate
     * @return the node id of the coordinate
     * @throws RuntimeException if coordinate doesn't represent any node from the graph
     */
    public int getIndexOfNode(double[] lonlat){
        //TODO: get index of coordinates
        for (int i = 0; i < locationList.length; i++) {
            if (lonlat[0] == this.getLongitudeOfNode(i) && lonlat[1] == this.getLatitudeOfNode(i)){
                System.out.println("ID: "+i);
                return i;
            }
        }
        throw new RuntimeException();
    }

}