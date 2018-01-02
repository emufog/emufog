package emufog.placement;

import emufog.graph.Graph;
import emufog.nodes.FogNode;
import emufog.settings.Settings;

public abstract class FogLayout {

    public abstract Graph createFogLayout(Graph graph, Settings settings, FogNode[] fogNodes);

}
