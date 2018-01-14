package emufog.export;

import com.google.common.graph.MutableNetwork;
import emufog.topology.Link;
import emufog.topology.Node;

import java.io.IOException;
import java.nio.file.Path;

public interface ITopologyExporter {

    void exportTopology(MutableNetwork<Node, Link> topology, Path path) throws IOException;
}
