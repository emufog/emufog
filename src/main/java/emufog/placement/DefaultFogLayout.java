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

    private Logger logger = Logger.getInstance();

    private List<Router> edgeRouters = new ArrayList<>();

    // Key represents number of ways to reach edge nodes under given limitations.
    private Map<Router, AtomicInteger> backboneRouterCoverage = new HashMap<>();

    private Map<Router, Set<Router>> coveredEdgeRouters = new HashMap<>();

    private List<FogNodeType> fogNodeTypes = getSettings().getFogNodeTypes();

    private AtomicInteger remainingNodes = new AtomicInteger(getSettings().getMaxFogNodes());

    private float threshold = getSettings().getCostThreshold();

    float delayBoundary = 50;

    @Override
    public void identifyFogNodes(MutableNetwork topology) {

        //get edgeRouters from stream of nodes
        topology.nodes()
                .parallelStream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(EDGE_ROUTER))
                .forEach(n -> edgeRouters.add((Router) n));

        //initialise list of backbone routers
        topology.nodes()
                .parallelStream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER))
                .forEach(n ->
                        backboneRouterCoverage.put(((Router) n), new AtomicInteger()));
        //initialise list of backbone routers
        topology.nodes()
                .parallelStream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER))
                .forEach(n ->
                        coveredEdgeRouters.put(((Router) n), new HashSet<>()));

        logger.log("# Backbone Routers: " + backboneRouterCoverage.size(), LoggerLevel.ADVANCED);
        logger.log("# Edge Routers: " + edgeRouters.size(), LoggerLevel.ADVANCED);
        logger.log(String.format("# Edge Routers with connected devices: %d", edgeRouters.stream()
                .filter(Router::hasDevices)
                .count()), LoggerLevel.ADVANCED);
        logger.log(String.format("# Edge devices: %d", topology.nodes()
                .parallelStream()
                .filter(node -> node instanceof Device)
                .count()), LoggerLevel.ADVANCED);

        while (!edgeRouters.isEmpty()) {
            determinePossibleFogNodes();
            Router connectionPoint = getBackboneNodeWithHighestEdgeCoverage();
            FogNode fogNodeToPlace = new FogNode(findCostOptimalFogNodeType(connectionPoint));
            if(remainingNodes.intValue() > 0){
                placeFogNode(connectionPoint, fogNodeToPlace);
                remainingNodes.getAndDecrement();
                edgeRouters.removeAll(coveredEdgeRouters.get(connectionPoint));
            }

        }
    }

    private void determinePossibleFogNodes() {

        determineCandidateRouters();
        logger.log(coveredEdgeRouters.toString());

        for (Map.Entry<Router, Set<Router>> entry : coveredEdgeRouters.entrySet()) {
            logger.log("Id" + entry.getKey().getID());
            for (Map.Entry<Router, Set<Router>> setEntry : coveredEdgeRouters.entrySet()) {
                logger.log(String.valueOf(setEntry.getValue().size()));
                if (setEntry.getValue() != null) {
                    for (Router router : setEntry.getValue()) {
                        logger.log("Router ID: " + router.getID());
                    }
                }
            }
        }

    }

    private void determineCandidateRouters() {

        float delay = 0;

        for (Router edgeRouter : edgeRouters) {

            AtomicInteger range = new AtomicInteger();

            //check if latency(v,a) ≤ latencyBoundary for all v ∈ Routers to a ∈ Backbone Routers
            for (Object neighbor : getTopology().adjacentNodes(edgeRouter)) {

                if (neighbor instanceof Router
                        && ((Router) neighbor).getType().equals(BACKBONE_ROUTER)) {

                    range.getAndIncrement();

                    Link link = checkNotNull(getTopology().edgeConnectingOrNull(edgeRouter, (Node) neighbor));

                    delay += link.getDelay();


                    if (delay <= delayBoundary) {

                        BitSet visited = new BitSet();
                        visited.set(((Router) neighbor).getID());

                        addBackboneRouter(((Router) neighbor), edgeRouter);

                        while (delay <= delayBoundary && range.intValue() < threshold) {

                            Router nextRouter = null;

                            for (Object n : getTopology().adjacentNodes((Node) neighbor)) {

                                if (range.intValue() < threshold) {
                                    if (n instanceof Router
                                            && ((Router) n).getType().equals(BACKBONE_ROUTER)) {

                                        if (!visited.get(((Router) n).getID())) {

                                            nextRouter = (Router) n;
                                            range.getAndIncrement();
                                            visited.set(nextRouter.getID());

                                            Link l = checkNotNull(getTopology()
                                                    .edgeConnectingOrNull(nextRouter, (Node) neighbor));

                                            delay += l.getDelay();
                                            if (delay <= delayBoundary) addBackboneRouter(nextRouter, edgeRouter);
                                        }
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
                }
                range.set(0);
            }

        }


    }

    /**
     * Add Backbone Router to possibleCandiateRouter data structure.
     *
     * @param router
     * @param edgeRouter
     */
    private void addBackboneRouter(Router router, Router edgeRouter) {
        try {
            backboneRouterCoverage.get(router).incrementAndGet();
            //Add current edge router to list of covered edge routers for current backbone router
            try {
                Set<Router> edgeRouters = coveredEdgeRouters.get(router);
                edgeRouters.add(edgeRouter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.log(e.toString());
        }

    }

    private FogNodeType findCostOptimalFogNodeType(Router connectionPoint) {


        Set<Router> routers = coveredEdgeRouters.get(connectionPoint);

        FogNodeType fogNodeType;

        fogNodeType = fogNodeTypes
                .stream()
                .min((fogNodeType1, fogNodeType2) ->
                        calculateRatio(connectedDevices(routers), fogNodeType1) > calculateRatio(connectedDevices(routers), fogNodeType2)
                                ? 1 : -1)
                .get();

        if (fogNodeType != null) {
            Logger.getInstance().log(fogNodeType.getName() + " was selected");
            return fogNodeType;
        } else {
            Logger.getInstance().log("There is no suitable fog node type.");
            return null;
        }
    }

    private int connectedDevices(Set<Router> routers) {
        int connectedDevices = 0;
        for (Router router : routers) {
            connectedDevices += router.getDeviceCount();
        }
        Logger.getInstance().log("Connected Devices: " + connectedDevices);
        return connectedDevices;
    }

    private double calculateRatio(int connectedDevices, FogNodeType fogNodeType) {
        double ratio = (connectedDevices - fogNodeType.getMaximumConnections()) / fogNodeType.getCosts();
        Logger.getInstance().log("ratio: " + ratio + " for " + fogNodeType.getId());
        return ratio;
    }

    /**
     * Returns the first router with highest edge router coverage.
     *
     * @return
     */
    private Router getBackboneNodeWithHighestEdgeCoverage() {
        return backboneRouterCoverage.entrySet()
                .stream()
                .max((entry1, entry2) -> entry1.getValue().intValue() > entry2.getValue().intValue() ? 1 : -1)
                .get()
                .getKey();
    }


    private void placeFogNode(Node node, FogNode fogNode) {
        getTopology().addNode(fogNode);
        Link link = new Link(fogNode.getFogNodeType().getNodeLatency(), fogNode.getFogNodeType().getNodeBandwidth());
        getTopology().addEdge(node, fogNode, link);
    }

    class FogResult {

        private boolean success;

        final Map<Router, FogNode> fogNodePlacements = new HashMap<>();

        void clearFogNodes() {
            fogNodePlacements.clear();
        }

        void success(boolean value) {
            this.success = value;
        }
    }

}
