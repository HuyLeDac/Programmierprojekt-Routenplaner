import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;
/**
 * Java server class for the web-based route planner.
 * 
 * @author Omar Aboulgadayel, Florian Strohmaier, Huy Le Dac
 *
 */
public class MapServer {

  public static void main(String[] args) throws IOException {
	long start = System.currentTimeMillis();
	System.out.println("Building Server with the necessary components...");
    com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8080), 0);
    Graph graph = new Graph(new File(args[1]));
    System.out.println("finished");
    long finish = System.currentTimeMillis();
    System.out.println("Bulding server took " + (finish-start) + "ms");
    
    server.createContext("/", new FileHandler());
    server.createContext("/request", new RequestHandler(graph));
    server.setExecutor(null);
    server.start();
  }
  
  
  /**
   * This class, which inherits the HttpHandler class, handles the HTML-file by uploading 
   * it on http://localhost:8080/
   *
   */
  static class FileHandler implements HttpHandler {
	
	/**
	 * This method just reads the HTML-file and prints it on http://localhost:8080/
	 */
    @Override
    public void handle(HttpExchange t) throws IOException {
      byte[] bytes = Files.readAllBytes(Paths.get("index.html"));
      t.sendResponseHeaders(200, bytes.length);
      OutputStream os = t.getResponseBody();
      os.write(bytes);
      os.close();
    }
  }
  
  /**
   * This class, which inherits the HttpHandler class, handles incoming AJAX request from 
   * the client and responses depending on the algotype parameter.
   * 
   */
  static class RequestHandler implements HttpHandler {
	
	//represents graph object
	Graph graph;
	
	//saves the different paramters
	HashMap<String, String> parameters;
	
	/**
	 * Constructor of the RequestHandler class
	 * 
	 * @param graph the graph object
	 */
	public RequestHandler(Graph graph) {
		this.graph = graph;
		this.parameters = new HashMap<>();
	}
	
	/**
	 * This method handles incoming AJAX requests by extracting the parameters first and
	 * after that responding differently dependent on the algotype parameter:
	 * 
	 * if dijkstra: calculating the shortest path between two coordinates given by the request
	 * if next node: calculating the nearest node of the coordinate given by the request
	 * else: responds with "Unknown algorithm type: " + algoType 
	 * 
	 * example: algotype=dijkstra&start=LatLng(48.779755, 9.19487)&target=LatLng(48.766179, 9.167061)
	 * "algotype" is "dijkstra"
	 * "start" is "48.779755, 9.19487"
	 * "target" is "48.766179, 9.167061"
	 * -> Uses dijkstra algorithm and adds a "&" and the nodeIDs of start and target
	 * 
	 */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();
        String response = "";
        HashMap<String, String> parameters = new HashMap<>();
        boolean shutdown = false;
        
        if (query != null) {
            
            String[] params = query.split("&");
            String algoType = "";
            String start = "";
            String target = "";
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("algotype")) {
                    algoType = keyValue[1];
                    parameters.put(keyValue[0], keyValue[1]);
                } else if (keyValue[0].equals("start")) {
                    start = keyValue[1];
                    parameters.put(keyValue[0], keyValue[1]);
                } else if (keyValue[0].equals("target")) {
                    target = keyValue[1];
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
            
            //response based on query parameters
            if (algoType.equals("dijkstra")) {
                System.out.println("Using Dijkstra algorithm, finding path from " + start + " to " + target);
                double[] src = {getLon(start), getLat(start)};
                double[] trg = {getLon(target), getLat(target)};
                int srcIndex = graph.getIndexOfNode(src);
                int trgIndex = graph.getIndexOfNode(trg);
                response = graph.getPathCoords(srcIndex, trgIndex).toString() + "&" + srcIndex + "&" + trgIndex;
                System.out.println("response: " + response + "&" + srcIndex + "&" + trgIndex);

            } else if (algoType.equals("nextNode")) {
                System.out.println("Using nearestNode algorithm, getting nearest node from " + start);
                double[] result = graph.getClosestDistance(getLon(start), getLat(start));
                response = Arrays.toString(result) + "&" + graph.getIndexOfNode(result);
                System.out.println("response: " + response + "&" + graph.getIndexOfNode(result));

            } else if (algoType.equals("exit")) {
                System.out.println("Shutting down...");
                shutdown = true;
                response = "Server stopped.";

            } else {
                //should never occur
                response = "Unknown algorithm type: " + algoType;
                System.out.println("response: " + response);
            }
        }
        
        //response
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

        if (shutdown){
            System.exit(0);
        }
    }
    
    
   /**
   * This method gets the latitude of the given coordinate string
   * 
   * @param node a coordinate 
   * @return latitude of the coordinate
   */
    private double getLat(String node) {
    	String[] arr = node.split(",");
    	return Double.parseDouble(arr[0]);
    }
    
    
    /**
     * This method gets the longitude of the given coordinate string
     * 
     * @param node a coordinate
     * @return longitude of the coordinate
     */
    private double getLon(String node) {
    	String[] arr = node.split(",");
    	return Double.parseDouble(arr[1]);
    }


  }
}
