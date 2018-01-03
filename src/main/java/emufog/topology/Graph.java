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
        return null;
    }

    public static Graph createFogLayout(FogLayout fogLayout, Graph graph){
        return null;
    }

    public Graph assignApplications(){
        return null;
    }

    private Graph identifyBackbone(){
        return null;
    }

    private Graph assignEdgeDevices(){
        return null;
    }


}
