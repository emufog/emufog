package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.DeviceNodeType;
import emufog.topology.Device;
import emufog.topology.Link;
import emufog.topology.Router;
import static emufog.topology.Types.RouterType.ROUTER;

import java.util.List;
import java.util.Random;

public class DefaultDevicePlacement implements IDevicePlacement {

    private List<Router> edgeRouters;

    @Override
    public void assignEdgeDevices(MutableNetwork topology, List<DeviceNodeType> deviceNodeTypes) {

        edgeRouters = (List<Router>) topology.nodes().stream().filter(node -> node instanceof Router).filter(node -> ((Router) node).getType().equals(ROUTER));

        Random random = new Random();

        for(DeviceNodeType type : deviceNodeTypes){
            float upper = Math.abs(type.getAverageDeviceCount() * 2);

            for(Router router : edgeRouters){
                int count = (int) (random.nextFloat() * upper);

                for(int i = 0; i < count; ++i){
                    Device device = new Device(count, router.getAsID(), type);
                    topology.addNode(device);

                }
            }
        }


    }
}
