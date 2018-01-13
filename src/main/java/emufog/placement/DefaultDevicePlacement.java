package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.graph.UniqueIPProvider;
import emufog.settings.Settings;
import emufog.topology.Link;
import emufog.util.UniqueIDProvider;
import emufog.nodeconfig.DeviceNodeType;
import emufog.topology.Device;
import emufog.topology.Router;
import static emufog.topology.Types.RouterType.ROUTER;

import java.util.List;
import java.util.Random;

public class DefaultDevicePlacement implements IDevicePlacement {

    private List<Router> edgeRouters;

    @Override
    public void assignEdgeDevices(MutableNetwork topology, List<DeviceNodeType> deviceNodeTypes) throws Exception {

        Settings settings = Settings.getInstance();

        edgeRouters = (List<Router>) topology.nodes().stream().filter(node -> node instanceof Router).filter(node -> ((Router) node).getType().equals(ROUTER));

        Random random = new Random();

        for(DeviceNodeType type : deviceNodeTypes){
            float upper = Math.abs(type.getAverageDeviceCount() * 2);

            UniqueIDProvider idProvider = UniqueIDProvider.getInstance();

            for(Router router : edgeRouters){
                int count = (int) (random.nextFloat() * upper);

                for(int i = 0; i < count; ++i){
                    Device device = new Device(idProvider.getNextID(), router.getAsID(), type);
                    topology.addNode(device);
                    Link link = new Link(idProvider.getNextID(), settings.getEdgeDeviceDelay(),settings.getEdgeDeviceBandwidth());
                    topology.addEdge(device, router, link);



                }
            }
        }


    }
}
