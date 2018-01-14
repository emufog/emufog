package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.FogNodeType;
import emufog.settings.Settings;
import emufog.topology.FogNode;
import emufog.topology.Link;
import emufog.topology.Router;
import emufog.util.UniqueIDProvider;

import static emufog.topology.Types.RouterType.ROUTER;

import java.util.List;

public class DefaultFogLayout implements IFogLayout{

    private List<Router> edgeRouters = null;

    private List<FogNodeType> fogNodeTypes = null;

    @Override
    public void identifyFogNodes(MutableNetwork topology) throws Exception {

        // get fog types from settings
        fogNodeTypes.addAll(Settings.getInstance().getFogNodes());

        //get edgeRouters from stream of nodes
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(ROUTER))
                .forEach(n -> edgeRouters.add((Router) n));

            //add fog node to each edgeRouter
            for(Router edgeRouter : edgeRouters){
                FogNode fogNode = new FogNode(UniqueIDProvider.getInstance().getNextID(), edgeRouter.getAsID(), fogNodeTypes.get(0));
                topology.addNode(fogNode);
                //TODO: Maybe introduce FogLink Bandwidth?
                Link link = new Link(UniqueIDProvider.getInstance().getNextID(), Settings.getInstance().getEdgeDeviceDelay(), Settings.getInstance().getEdgeDeviceBandwidth());
                topology.addEdge(fogNode, edgeRouter, link);
            }
        }
}
