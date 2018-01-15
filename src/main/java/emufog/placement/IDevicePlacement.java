package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.DeviceNodeType;

import java.util.List;

public interface IDevicePlacement {

    void assignEdgeDevices(MutableNetwork topology, List<DeviceNodeType> deviceNodeTypes) throws Exception;
}
