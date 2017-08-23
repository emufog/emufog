package emufog.reader;

import emufog.graph.Graph;
import emufog.settings.Settings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract class providing the settings required for all input readers.
 */
public abstract class GraphReader {

    /* the settings to use for the read in graph */
    protected final Settings settings;

    /**
     * Creates a new instance associated with the given settings.
     *
     * @param settings settings to use for the new graph
     */
    GraphReader(Settings settings) {
        this.settings = settings;
    }

    /**
     * Reads in and returns a graph from a file base.
     *
     * @param files list of files to read in
     * @return the read in graph structure
     * @throws IOException              throws exception in case there is a problem with reading in the respective file
     * @throws IllegalArgumentException throws exception if given file list is invalid
     */
    public abstract Graph readGraph(List<Path> files) throws IOException, IllegalArgumentException;
}
