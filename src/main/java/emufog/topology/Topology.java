package emufog.topology;

import com.google.common.graph.MutableNetwork;

import java.util.Set;

public class Topology {

    private MutableNetwork<Node,Link> topology;

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

    private Topology(TopologyBuilder builder){

        read();
        identifyEdge();
        placeFogNodes();
        assignApplications();

    }

    private void read(){}

    private void identifyEdge(){}

    private void placeFogNodes(){}

    private void assignApplications(){}



    public static class TopologyBuilder{

        public Topology build(){
            return new Topology(this);
        }

        public TopologyBuilder setup(){
            return this;
        }

    }

    public void export(){}

}
