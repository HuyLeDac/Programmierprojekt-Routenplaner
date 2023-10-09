import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This class represents an object which solves different path problems of the specific Graph.
 */
public class PathFinder {
    /*
     * A graph with nodes and edges
     */
    private final Graph graph;
    /*
     * number of nodes of the graph
     */
    private final int numberOfNodes;
    /*
     * An array which stores all distances from the starting Point
     * to all nodes after the calculation
     */
    private int[] nodeDistance;
    /*
     * An array which remembers if the node was already visited or not
     */
    private boolean[] visited;

    /**
     * Constructor method of the PathFinder class.
     *
     * @param graph specific graph
     */
    
    private int[] previousNode;
    
    public PathFinder(Graph graph) {
        this.graph = graph;
        this.numberOfNodes = graph.getNumberOfNodes();
        this.previousNode = new int[numberOfNodes];
        for(int i = 0; i<numberOfNodes; i++) {
        	previousNode[i] = i;
        }
    }


    /**
     * This method represents the One To One Dijkstra algorithm
     *
     * @param startingPointID source index
     * @param endPointID      target index
     * @return the return value of the Dijkstra algortihm
     */
    public int oneToOneDijkstra(int startingPointID, int endPointID) {

        //A prority queue which sorts the nodes by their node distances
        PriorityQueue<int[]> pq = new PriorityQueue<>((v1, v2) -> v1[1] - v2[1]);
        //displays actual distance of the specific node
        nodeDistance = new int[numberOfNodes];
        //shows if the node was already visited
        visited = new boolean[numberOfNodes];
        
       
        for (int i = 0; i < numberOfNodes; i++) {
            nodeDistance[i] = Integer.MAX_VALUE;
            previousNode[i] = i;
        }

        nodeDistance[startingPointID] = 0;
        previousNode[startingPointID] = startingPointID;
        int[] startingPoint = {startingPointID, nodeDistance[startingPointID]};
        pq.add(startingPoint);

        while (!pq.isEmpty()) {

            if (visited[endPointID] == true) {
                return nodeDistance[endPointID];
            }

            //Remove predecessor node with lowest nodeDistance from queue
            int[] predecessor = pq.poll();

            if (!visited[predecessor[0]]) {

                /*
                 * Add successor into priority queue and Update Node values.
                 */
                update(predecessor, pq);


            }
        }

        if (visited[endPointID] == true) {
            return nodeDistance[endPointID];
        } else {
            System.out.println("route doesn't exist");
            return Integer.MAX_VALUE;
            
        }

    }

    /**
     * This method represents the One to All Dijkstra algorithm from the
     * source node to all other nodes
     *
     * @param startingPointID
     * @return An array which contains the distances from the specific starting
     * point to all other nodes
     */
    public int[] oneToAllDijkstra(int startingPointID) {

        //A prority queue which sorts the nodes by their node distances
        PriorityQueue<int[]> pq = new PriorityQueue<>((v1, v2) -> v1[1] - v2[1]);
        //displays actual distance of the specific node
        nodeDistance = new int[numberOfNodes];
        //shows if the node was already visited
        visited = new boolean[numberOfNodes];

        for (int i = 0; i < numberOfNodes; i++) {
            nodeDistance[i] = Integer.MAX_VALUE;
            previousNode[i] = i;
        }

        nodeDistance[startingPointID] = 0;
        int[] startingPoint = {startingPointID, nodeDistance[startingPointID]};
        pq.add(startingPoint);

        while (!pq.isEmpty()) {

            //Remove predecessor node with lowest nodeDistance from queue
            int[] predecessor = pq.poll();

            if (!visited[predecessor[0]]) {

                /*
                 * Add successor into priority queue and Update Node values.
                 */
                update(predecessor, pq);
                visited[predecessor[0]] = true;

            }
        }

        return nodeDistance;
    }

    /**
     * Updates the nodes which are connected to the predecessor and adds them to the queue
     * in case it's not visited.
     *
     * @param predecessor the source node and distance value
     * @param pq          the given priority queue
     */
    private void update(int[] predecessor, PriorityQueue<int[]> pq) {

        visited[predecessor[0]] = true;
        int firstEdgeIDX = graph.getOffsetOfNode(predecessor[0]);
        int numberOfOutgoingEdges = graph.getOffsetOfNode(predecessor[0] + 1);

        for (int i = firstEdgeIDX; i < numberOfOutgoingEdges; i++) {

            //checks whether the weight of the successors should be updated or not
            if (!visited[graph.getTrgIDXofEdge(i)]) {
                if (nodeDistance[predecessor[0]] + graph.getWeightOfEdge(i) < nodeDistance[graph.getTrgIDXofEdge(i)]) {
                    nodeDistance[graph.getTrgIDXofEdge(i)] = nodeDistance[predecessor[0]] + graph.getWeightOfEdge(i);
                    int[] arr = {graph.getTrgIDXofEdge(i), nodeDistance[graph.getTrgIDXofEdge(i)]};
                    pq.add(arr);
                    previousNode[graph.getTrgIDXofEdge(i)] = graph.getSrcIDXofEdge(i);
                }
            }
        }
    }
    
    
    /**
     * Calculates and returns a list of nodes which are in the shortest path between two nodes in the exact order.
     * 
     * @param startingPointID start id
     * @param endPointID target id
     * @return A list of nodes which are in the shortest path of two nodes (start last, target id first).
     */
    public LinkedList<Integer> pathFromAToB(int startingPointID, int endPointID) {
    	int distance = this.oneToOneDijkstra(startingPointID, endPointID);
    	LinkedList<Integer> nodeList = new LinkedList();
    	int currentNode = endPointID;
    	if(distance == Integer.MAX_VALUE){
    		return nodeList;
    	}
    	
    	
    	while(currentNode != startingPointID) {
    		nodeList.add(currentNode);
    		currentNode = previousNode[currentNode];
    	}
    	nodeList.add(startingPointID);
    	
    	return nodeList;
    }
   
    
   /**
    * Calculates and returns a list of coordinates which are in the shortest path between two nodes in the exact order.
    * 
    * @param startingPointID start id
    * @param endPointID target id
    * @return A list of coordinates (lon, lat) which are in the shortest path of two nodes (start coordinate last, target coordinate first).
    */
   public LinkedList<String> pathFromAToBCoords(int startingPointID, int endPointID){
    	LinkedList<Integer> nodeList = this.pathFromAToB(startingPointID, endPointID);
    	LinkedList<String> nodeListCoord = new LinkedList();
    	if(nodeList.isEmpty()) {
    		nodeListCoord.add("null");
    		return nodeListCoord;
    	}else {
    		for(int i = 0; i<nodeList.size(); i++) {
    			int currentNode = i;
    			double[] currentNodeCoord = new double[2];
    			currentNodeCoord[0] = graph.getLongitudeOfNode(nodeList.get(currentNode));
        		currentNodeCoord[1] = graph.getLatitudeOfNode(nodeList.get(currentNode));
        		nodeListCoord.add(Arrays.toString(currentNodeCoord));
    		}
    	}
    	
		return nodeListCoord;
    }
   
}