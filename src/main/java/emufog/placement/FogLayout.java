package emufog.placement;

import emufog.application.Application;
import emufog.nodes.FogNode;
import emufog.settings.Settings;
import emufog.topology.Topology;

public abstract class FogLayout {

    public abstract Topology createFogLayout(Topology topology, Settings settings, FogNode[] fogNodes);

    public abstract void applicationMapping(Application[] applications);

}
