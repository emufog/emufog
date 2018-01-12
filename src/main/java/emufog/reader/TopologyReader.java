package emufog.reader;

import com.google.common.graph.MutableNetwork;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public abstract class TopologyReader {

    public abstract MutableNetwork parse(Path path) throws FileNotFoundException, IOException;

}
