package emufog.reader;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class BriteReader extends TopologyReader{

    MutableNetwork<Node,Link> topology = NetworkBuilder.undirected().allowsParallelEdges(true).build();

    private BufferedReader reader;

    /**
     * Creates a new topology node from detected BRITE node.
     * @param reader
     * @throws IOException
     */

    private void extractNodes(BufferedReader reader) throws IOException {

        String line = reader.readLine();

        while (line != null && !line.isEmpty()){
            String[] values = line.split("\t");

            if(values.length >= 7){
                int id = Integer.parseInt(values[0]);
                int asID = Integer.parseInt(values[5]);
                Router router = new Router(id, asID);
                topology.addNode(router);
            }

            line = reader.readLine();
        }
    }

    /**
     * Creates a new link in the topology from detected BRITE edge;
     * @param reader
     * @throws IOException
     */

    private void extractEdges(BufferedReader reader) throws IOException {

        String line = reader.readLine();

        while (line !=null && !line.isEmpty()){
            String[] values = line.split("\t");

            if(values.length >= 9){
                int id = Integer.parseInt(values[0]);
                int from = Integer.parseInt(values[1]);
                int to = Integer.parseInt(values[2]);
                float delay = Float.parseFloat(values[4]);
                float bandwidth = Float.parseFloat(values[5]);

                Link l = new Link(id,delay,bandwidth);

                Node[] nodes;
                nodes = new Node[]{
                        (Node) topology.nodes().stream().filter(node -> node.getID() == from),
                        (Node) topology.nodes().stream().filter(node -> node.getID() == to)};

                topology.addEdge(nodes[0],nodes[1],l);
            }

            line = reader.readLine();
        }
    }

    @Override
    public MutableNetwork parse(Path path) throws IOException{

        reader = new BufferedReader(new FileReader(path.toFile()));

        String currentLine = reader.readLine();

        while (currentLine != null) {
            if(currentLine.startsWith("Nodes:")) extractNodes(reader);
            if(currentLine.startsWith("Edges:")) extractEdges(reader);

            currentLine = reader.readLine();
        }

        return topology;
    }
}
