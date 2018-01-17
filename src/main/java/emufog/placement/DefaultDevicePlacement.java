package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.DeviceNodeType;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.Link;
import emufog.topology.Router;
import emufog.util.UniqueIDProvider;
import emufog.util.UniqueIPProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static emufog.topology.Types.RouterType.ROUTER;

public class DefaultDevicePlacement implements IDevicePlacement {

    private List<Router> edgeRouters = new ArrayList<>();


    /**
     * Assigns the devices specified in the settings to the edge nodes on a random base.
     */
    @Override
    public void assignEdgeDevices(MutableNetwork topology, List<DeviceNodeType> deviceNodeTypes) throws Exception {

        Settings settings = Settings.getInstance();

        // TODO: Validate stream performance, could be slow. Eventually experiment with parallelStreams.
        //get stream of nodes filter for edge routers and add them to edgeRouters list.
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(ROUTER))
                .forEach(n -> edgeRouters.add((Router)n));


        Random random = new Random();

        for(DeviceNodeType type : deviceNodeTypes){
            float upper = Math.abs(type.getAverageDeviceCount() * 2);

            for(Router router : edgeRouters){
                int count = (int) (random.nextFloat() * upper);

                for(int i = 0; i < count; ++i){

                    Device device = new Device(UniqueIDProvider.getInstance().getNextID(), router.getAsID(), type);

                    //TODO: Implement auto mark as used in UniqueIDProvider. Architectural problem: Due to singleton id's are assigned to nodes are increasing globally. FogNodes not starting at 0 but at lowest avail id. Maybe confusing.
                    //very important! mark each generated id as used. The UniqueIDProvider doesnt take care of this.
                    UniqueIDProvider.getInstance().markIDused(device.getID());

                    DeviceNodeConfiguration deviceNodeConfiguration = new DeviceNodeConfiguration(UniqueIPProvider.getInstance().getNextIPV4Address());

                    device.setConfiguration(deviceNodeConfiguration);

                    topology.addNode(device);
                    Link link = new Link(UniqueIDProvider.getInstance().getNextID(), settings.getEdgeDeviceDelay(),settings.getEdgeDeviceBandwidth());
                    topology.addEdge(device, router, link);

                    //Logger.getInstance().log(ReflectionToStringBuilder.toString(device, ToStringStyle.MULTI_LINE_STYLE));
                }


            }
        }
    }
}
