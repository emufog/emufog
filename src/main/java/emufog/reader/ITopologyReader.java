package emufog.reader;

import com.google.common.graph.Graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ITopologyReader {

      Graph read(List<Path> files) throws IOException, IllegalArgumentException;
}
