package emufog.reader;

import com.google.common.graph.Graph;

import java.io.IOException;

public interface ITopologyReader {

      Graph read(String files) throws IOException, IllegalArgumentException;
}
