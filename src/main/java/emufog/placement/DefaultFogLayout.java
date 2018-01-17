package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.FogNodeConfiguration;
import emufog.nodeconfig.FogNodeType;
import emufog.settings.Settings;
import emufog.topology.FogNode;
import emufog.topology.Link;
import emufog.topology.Router;
import emufog.util.UniqueIDProvider;
import emufog.util.UniqueIPProvider;

import java.util.ArrayList;
import java.util.List;

import static emufog.topology.Types.RouterType.ROUTER;

public class DefaultFogLayout implements IFogLayout{

    private List<Router> edgeRouters = new ArrayList<>();

    private List<FogNodeType> fogNodeTypes = new ArrayList<>();

    @Override
    public void identifyFogNodes(MutableNetwork topology) throws Exception {

        // get fog types from settings
        fogNodeTypes = (Settings.getInstance().getFogNodeTypes());

        //get edgeRouters from stream of nodes
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(ROUTER))
                .forEach(n -> edgeRouters.add((Router) n));

            //add fog node to each edgeRouter
            for(Router edgeRouter : edgeRouters){

                FogNode fogNode = new FogNode(UniqueIDProvider.getInstance().getNextID(), edgeRouter.getAsID(), fogNodeTypes.get(0));

                UniqueIDProvider.getInstance().markIDused(fogNode.getID());

                FogNodeConfiguration fogNodeConfiguration = new FogNodeConfiguration(UniqueIPProvider.getInstance().getNextIPV4Address());
                fogNode.setConfiguration(fogNodeConfiguration);


                topology.addNode(fogNode);
                //TODO: Maybe introduce FogLink Bandwidth?
                Link link = new Link(UniqueIDProvider.getInstance().getNextID(), Settings.getInstance().getEdgeDeviceDelay(), Settings.getInstance().getEdgeDeviceBandwidth());
                topology.addEdge(fogNode, edgeRouter, link);
            }
        }
}
