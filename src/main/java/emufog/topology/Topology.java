package emufog.topology;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import emufog.reader.BriteReader;
import emufog.reader.ITopologyReader;
import emufog.settings.Settings;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class Topology {

    private MutableNetwork<Node,Link> topology;

    private static Settings settings;

    private void addNode(Node node){
        topology.addNode(node);
    }

    private void removeNode(Node node) {topology.removeNode(node);}

    private void addLink(Node nodeU, Node nodeV, Link link){
        topology.addEdge(nodeU,nodeV,link);
    }

    private void removeLink(Link link){ topology.removeEdge(link);}

    private void addFogNode(FogNode fogNode){
        topology.addNode(fogNode);
    }

    private void addDeviceNode(Device device){
        topology.addNode(device);
    }

    private void addRouter(Router router){
        topology.addNode(router);
    }

    public Set<Node> nodes(){ return topology.nodes(); }

    public Set<Link> links(){
        return topology.edges();
    }

    public final MutableNetwork<Node, Link> build(){

        identifyEdge();
        placeFogNodes();
        assignApplications();

        return topology;
    };


    public void setup(Settings settings){
        this.settings = checkNotNull(settings);
    }

    public void read(){

        ITopologyReader reader = new BriteReader();

        topology = NetworkBuilder.undirected().allowsParallelEdges(true).build();

        reader.read();


    }

    private void identifyEdge(){


    }

    private void placeFogNodes(){}

    private void assignApplications(){}

}
