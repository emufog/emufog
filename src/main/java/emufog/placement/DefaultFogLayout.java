package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.FogNodeType;
import emufog.topology.*;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;
import static emufog.settings.Settings.getSettings;
import static emufog.topology.Topology.getTopology;
import static emufog.topology.Types.RouterType.BACKBONE_ROUTER;
import static emufog.topology.Types.RouterType.EDGE_ROUTER;

public class DefaultFogLayout implements IFogLayout {

    Logger logger = Logger.getInstance();

    private List<Router> edgeRouters = new ArrayList<>();

    private List<FogNodeType> fogNodeTypes = new ArrayList<>();

    private AtomicInteger remainingNodes = new AtomicInteger(getSettings().getMaxFogNodes());

    private float threshold = getSettings().getCostThreshold();

    private List<Router> possibleFogNodePlacements = new ArrayList<>();

    Map<FogNodeType, List<FogNode>> fogPlacements;

    //TODO Add field to settings.
    private float delayBoundary = 100;

    @Override
    public void identifyFogNodes(MutableNetwork topology) throws Exception {

        // get fog types from settings
        fogNodeTypes = (getSettings().getFogNodeTypes());

        //get edgeRouters from stream of nodes
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(EDGE_ROUTER))
                .forEach(n -> edgeRouters.add((Router) n));

        logger.log("# Edge Routers: " + edgeRouters.size(), LoggerLevel.ADVANCED);
        logger.log("# Edge Routers with connected devices: " +
                edgeRouters.stream()
                        .filter(node -> node.hasDevices())
                        .count(), LoggerLevel.ADVANCED);
        logger.log("# Edge devices: " +
                topology.nodes()
                        .stream()
                        .filter(node -> node instanceof Device)
                        .count(), LoggerLevel.ADVANCED);

        determinePossibleFogNodes();


    }

    private void determinePossibleFogNodes() {

        Map<Integer, List<Router>> candidateRouters = determineCandidateRouters();

        logger.log("Size" + candidateRouters.entrySet().size(), LoggerLevel.ADVANCED);

    }

    private Map<Integer, List<Router>> determineCandidateRouters() {

        float delay = 0;

        Map<Integer, List<Router>> candidateRouters = new HashMap<>();

        for (Router edgeRouter : edgeRouters) {

            List<Router> routerList = new ArrayList<>();

            int range = 0;

            //check if latency(v,a) ≤ latencyBoundary for all v ∈ Routers
            for (Object neighbor : getTopology().adjacentNodes(edgeRouter)) {

                if (neighbor instanceof Router && ((Router) neighbor).getType().equals(BACKBONE_ROUTER)) {

                    range++;

                    Link link = checkNotNull(getTopology().edgeConnectingOrNull(edgeRouter, (Node) neighbor));

                    delay += link.getDelay();


                    if (delay <= delayBoundary) {

                        BitSet visited = new BitSet();

                        routerList.add((Router) neighbor);
                        visited.set(((Router) neighbor).getID());

                        while (delay <= delayBoundary && range <= threshold) {

                            Router nextRouter = null;

                            for (Object n : getTopology().adjacentNodes((Node) neighbor)) {

                                range++;

                                if (range <= threshold) {
                                    if (n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER) && !visited.get(((Router) n).getID())) {
                                        nextRouter = (Router) n;
                                        visited.set(nextRouter.getID());

                                        Link l = checkNotNull(getTopology().edgeConnectingOrNull(nextRouter, (Node) neighbor));
                                        delay += l.getDelay();

                                        if (delay <= delayBoundary) routerList.add(nextRouter);
                                    }
                                }

                            }

                            if (nextRouter != null) {
                                neighbor = nextRouter;
                            } else {
                                break;
                            }
                        }

                        visited.clear();

                    }

                    candidateRouters.put(edgeRouter.getID(), routerList);
                    logger.log("Router List: " + routerList.size(), LoggerLevel.ADVANCED);

                }

                routerList.clear();
            }


        }

        return candidateRouters;

    }

    private void findCostOptimalFogNodeType(float threshold, Router router) {

        if (remainingNodes.get() >= 0) {


        }

    }


    private void placeFogNode(Node node, FogNode fogNode) {
        getTopology().addNode(fogNode);
        Link link = new Link(fogNode.getFogNodeType().getNodeLatency(), fogNode.getFogNodeType().getNodeBandwidth());
        getTopology().addEdge(node, fogNode, link);
    }

    class FogResult {

        private boolean success;

        final Map<Node, FogNode> nodeMap = new HashMap<>();

        void clearFogNodes() {
            nodeMap.clear();
        }

        void success(boolean value) {
            this.success = value;
        }


    }

    private boolean fogNodesLeft() {
        return remainingNodes.get() > 0;
    }

    void decrementRemainingNodes() {
        remainingNodes.decrementAndGet();
    }

}
