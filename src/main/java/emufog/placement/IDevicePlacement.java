package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.DeviceNodeType;

public interface IDevicePlacement {

    void assignEdgeDevices(MutableNetwork topology, DeviceNodeType[] deviceNodeTypes) throws Exception;
}
