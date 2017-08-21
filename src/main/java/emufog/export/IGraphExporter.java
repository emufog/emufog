package emufog.export;

import emufog.graph.Graph;

import java.nio.file.Path;

public interface IGraphExporter {

    /**
     * Exports the given graph object in an unspecified format.
     *
     * @param graph the graph to export
     * @param path  path to export the graph to
     * @throws IllegalArgumentException throws exception if the input parameters are invalid
     */
    void exportGraph(Graph graph, Path path) throws IllegalArgumentException;
}
