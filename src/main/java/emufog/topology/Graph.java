package emufog.topology;

import emufog.exceptions.EdgeError;
import emufog.placement.EdgeIdentifier;
import emufog.placement.FogLayout;
import emufog.reader.GraphReader;

import java.nio.file.Path;
import java.util.List;

public class Graph {

    public static Graph readInputGraph(GraphReader reader, List<Path> paths){ return null;}

    public static Graph identifyEdge(EdgeIdentifier edgeIdentifier, Graph graph) throws EdgeError{

        return graph;
    }

    public static Graph createFogLayout(FogLayout fogLayout, Graph graph){
        return graph;
    }

    public static Graph assignApplications(Graph graph){
        return graph;
    }

    private Graph identifyBackbone(){
        return null;
    }

}
