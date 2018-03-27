package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.DeviceNodeType;
import emufog.topology.Device;
import emufog.topology.Link;
import emufog.topology.Router;
import emufog.util.Logger;
import emufog.util.UniqueIDProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static emufog.topology.Types.RouterType.EDGE_ROUTER;

public class DefaultDevicePlacement implements IDevicePlacement {

    private List<Router> edgeRouters = new ArrayList<>();


    /**
     * Assigns the devices specified in the settings to the edge nodes on a random base.
     */
    @Override
    public void assignEdgeDevices(MutableNetwork topology, List<DeviceNodeType> deviceNodeTypes) throws Exception {

        //get stream of nodes filter for edge routers and add them to edgeRouters list.
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(EDGE_ROUTER))
                .forEach(n -> edgeRouters.add((Router) n));


        Random random = new Random();

        for (DeviceNodeType type : deviceNodeTypes) {
            float upper = Math.abs(type.getAverageDeviceCount() * 2);

            for (Router router : edgeRouters) {
                int count = (int) (random.nextFloat() * upper);

                for (int i = 0; i < count; ++i) {

                    Device device = new Device(router.getAsID(), type);

                    router.incrementDeviceCount();

                    DeviceNodeConfiguration deviceNodeConfiguration = new DeviceNodeConfiguration();

                    device.setConfiguration(deviceNodeConfiguration);

                    topology.addNode(device);
                    Link link = new Link(UniqueIDProvider.getInstance().getNextID(), device.getDeviceNodeType().getNodeLatency(), device.getDeviceNodeType().getNodeBandwidth());
                    topology.addEdge(device, router, link);
                }


            }
        }


        Logger logger = Logger.getInstance();
        logger.log("Placed "
                + topology
                .nodes()
                .stream()
                .filter(n -> n instanceof Device).count()
                + " devices in the Topology.\n");

        for(DeviceNodeType deviceNodeType : deviceNodeTypes){
            int count = (int) topology
                    .nodes()
                    .stream()
                    .filter(n -> n instanceof Device && ((Device) n).getDeviceNodeType().getName()
                            .equals(deviceNodeType.getName())).count();
            logger.log(count + " devices of type " + deviceNodeType.getName());
        }
        logger.log("");
    }
}
