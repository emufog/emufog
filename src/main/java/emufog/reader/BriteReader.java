package emufog.reader;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;
import emufog.util.Logger;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static emufog.topology.Types.RouterType.ROUTER;

public class BriteReader extends TopologyReader{

    private MutableNetwork<Node,Link> topology = NetworkBuilder.undirected().allowsParallelEdges(false).build();

    private BufferedReader reader;

    /**
     * Parses given input topology and returns MutableNetwork topology.
     *
     * @param path to input topology
     * @return MutableNetwork topology
     * @throws IOException if input doesnt exist.
     */
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

    /**
     * Creates a new topology node from parsed BRITE node.
     * @param reader Buffered reader to read lines from input file.
     * @throws IOException if input file doesnt exist.
     */

    private void extractNodes(BufferedReader reader) throws IOException {

        String line = reader.readLine();
        while (line != null && !line.isEmpty()){
            String[] values = line.split("\t");

            if(values.length >= 7){
                int id = Integer.parseInt(values[0]);
                int asID = Integer.parseInt(values[5]);
                Router router = new Router(id, asID);
                router.setType(ROUTER);
                topology.addNode(router);
            }

            line = reader.readLine();
        }

        for(Node node: topology.nodes()){
            Logger.getInstance().log(ReflectionToStringBuilder.toString(node, ToStringStyle.MULTI_LINE_STYLE));
        }


    }

    /**
     * Creates a new link in the topology from parsed BRITE edge;
     * @param reader Buffered reader to read lines from input file.
     * @throws IOException if input file doesnt exist.
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

                List<Node> incidentNodes = new ArrayList<>();

                Link l = new Link(id,delay,bandwidth);

                Logger.getInstance().log(ReflectionToStringBuilder.toString(l, ToStringStyle.MULTI_LINE_STYLE));

                for(Node node : topology.nodes()){

                    if(node.getID() == from){
                        incidentNodes.add(node);
                    }
                    if(node.getID() == to){
                        incidentNodes.add(node);
                    }
                }

                for(Node node : incidentNodes){
                    Logger.getInstance().log(ReflectionToStringBuilder.toString(node, ToStringStyle.MULTI_LINE_STYLE));
                }

                topology.addEdge(checkNotNull(incidentNodes.get(0)),checkNotNull(incidentNodes.get(1)),l);
            }

            line = reader.readLine();
        }

        for(Link link: topology.edges()){
            Logger.getInstance().log(ReflectionToStringBuilder.toString(link, ToStringStyle.MULTI_LINE_STYLE));

        }

    }
}

