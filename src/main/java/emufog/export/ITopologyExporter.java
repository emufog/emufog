package emufog.export;

import com.google.common.graph.MutableNetwork;

import java.io.IOException;
import java.nio.file.Path;

public interface ITopologyExporter {

    void exportTopology(MutableNetwork topology, Path path) throws IOException;
}
