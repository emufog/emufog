package export.emufog;

import graph.emufog.Graph;

public interface IGraphExporter {

    /**
     * Exports the given graph object in an unspecified format.
     *
     * @param graph the graph to export
     * @return true if exported successfully, false otherwise
     */
    boolean exportGraph(Graph graph);
}
