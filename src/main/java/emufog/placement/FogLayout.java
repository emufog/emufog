package emufog.placement;

import emufog.application.Application;
import emufog.settings.Settings;
import emufog.topology.Graph;

public abstract class FogLayout {

    public abstract Graph createFogLayout(Graph topology, Settings settings, FogNode[] fogNodes);

    public abstract void applicationMapping(Application[] applications);

}
