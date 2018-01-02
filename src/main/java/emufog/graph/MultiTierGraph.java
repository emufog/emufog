package emufog.graph;

import emufog.nodes.DeviceNode;
import emufog.nodes.FogNode;
import emufog.settings.Settings;

import java.util.List;

public class MultiTierGraph extends Graph{

    List<DeviceNode> deviceNodes;
    List<FogNode> fogNodes;

    /**
     * Creates a new basic graph instance.
     * Uses the given settings for the classification algorithms.
     *
     * @param settings settings to use for the graph
     * @throws IllegalArgumentException if the settings object is null
     */
    public MultiTierGraph(Settings settings) throws IllegalArgumentException {
        super(settings);

        deviceNodes = settings.getDeviceNodes();
        fogNodes = settings.getFogNodes();
    }


}
