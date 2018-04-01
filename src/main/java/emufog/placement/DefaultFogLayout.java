package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.nodeconfig.FogNodeType;
import emufog.topology.FogNode;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;
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
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(EDGE_ROUTER))
                .forEach(n -> edgeRouters.add((Router) n));

        //initialise list of backbone routers
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER))
                .forEach(n ->
                        backboneRouterCoverage.put(((Router) n), new AtomicInteger()));
        //initialise list of backbone routers
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER))
                .forEach(n ->
                        coveredEdgeRouters.put(((Router) n), new HashSet<>()));

        logger.log(String.format("# Edge Routers with connected devices: %d", edgeRouters.stream()
                .filter(Router::hasDevices)
                .count()), LoggerLevel.ADVANCED);

        logger.log("Available fog nodes: " + remainingNodes.intValue());


        while (!edgeRouters.isEmpty()) {

            if (remainingNodes.intValue() > 0) {

                determineCandidateRouters();

                Router connectionPoint = getBackboneNodeWithHighestEdgeCoverage();

                FogNode fogNodeToPlace = new FogNode(findCostOptimalFogNodeType(connectionPoint));

                placeFogNode(connectionPoint, fogNodeToPlace);

                //Only remove router if all connected devices are covered.
                for (Router coveredRouter : coveredEdgeRouters.get(connectionPoint)) {
                    if (coveredRouter.covered()) edgeRouters.remove(coveredRouter);
                }

                remainingNodes.getAndDecrement();

                logger.log("");
                logger.log("Placed Fog Node at Backbone Router "
                        + connectionPoint.getID()
                        + " \nwith Fog Type "
                        + fogNodeToPlace.getFogNodeType().getId()
                        + " covering " + coveredEdgeRouters.get(connectionPoint).size()
                        + " edge routers.");
                logger.log("Remaining fog nodes available: " + remainingNodes.intValue());
                logger.log("");

            } else {
                if (!edgeRouters.isEmpty()) {
                    logger.logSeparator();
                    logger.log(String.format("Fog Layout creation was not successful! " +
                            "\nThere are %d unconnected edge routers. " +
                            "\nCovering %d unconnected devices.", edgeRouters.size(), uncoveredDevices(edgeRouters)));
                    logger.logSeparator();
                    edgeRouters.clear();
                    System.exit(1);
                }
            }

        }

        logger.log("");
        logger.log("Placed "
                + topology.nodes().stream().filter(n -> n instanceof FogNode).
                count()
                + " Fog nodes in total in the topology.\n");
        for (FogNodeType fogNodeType : getSettings().getFogNodeTypes()) {
            int count = (int) topology
                    .nodes()
                    .stream()
                    .filter(n -> n instanceof FogNode && ((FogNode) n).getFogNodeType().getName()
                            .equals(fogNodeType.getName())).count();
            logger.log(count + " fog nodes of " + fogNodeType.getName());
        }
        logger.log("");
    }

    private boolean isBackboneRouter(Object n) {
        if (n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER)) {
            return true;
        } else return false;
    }

    private HashSet<Router> getAdjacentBackboneRouters(Router router) {
        HashSet<Router> adjacentBackboneRouters = new HashSet<>();

        for (Object adjacentRouter : getTopology().adjacentNodes(router)) {
            if (isBackboneRouter(adjacentRouter)) {
                adjacentBackboneRouters.add((Router) adjacentRouter);
            }
        }
        return adjacentBackboneRouters;
    }


    private void determineCandidateRouters() {

        float delay = 0;
        AtomicInteger range = new AtomicInteger();
        range.set(0);

        for (Router edgeRouter : edgeRouters) {

            Set<Router> adjacentBackboneRouters = new HashSet<>();

            List<Router> predecessors = new ArrayList<>();
            predecessors.add(edgeRouter);

            BitSet visited = new BitSet();


            //iterate over each predecessor
            for (int i = 0; i <= predecessors.size(); i++) {

                if (range.intValue() < threshold) {

                    range.getAndIncrement();
                    Router predecessor = predecessors.get(i);
                    // find adjacentBackboneRouters from current predecessor.
                    adjacentBackboneRouters = getAdjacentBackboneRouters(predecessor);

                    for (Router backboneRouter : adjacentBackboneRouters) {

                        Link link = checkNotNull(getTopology().edgeConnectingOrNull(predecessor, backboneRouter));
                        delay += link.getDelay();

                        if (delay <= delayBoundary) {
                            addBackboneRouter(backboneRouter, edgeRouter);
                        }
                    }

                    predecessors.addAll(adjacentBackboneRouters);
                    adjacentBackboneRouters.clear();
                    predecessors.remove(i);
                }
            }

            range.set(0);
        }

    }

    /**
     * Add Backbone Router to possibleCandidateRouter data structure.
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
                if (edgeRouter != null) edgeRouters.add(edgeRouter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Finde one suitable fog node type and adapt covered devices count in topology.
     *
     * @param connectionPoint
     * @return
     */
    private FogNodeType findCostOptimalFogNodeType(Router connectionPoint) {

        Set<Router> routers = coveredEdgeRouters.get(connectionPoint);

        FogNodeType fogType = fogNodeTypes
                .stream()
                .min((fogNodeType1, fogNodeType2) ->
                        calculateRatio(uncoveredDevices(routers), fogNodeType1) > calculateRatio(uncoveredDevices(routers), fogNodeType2)
                                ? 1 : -1)
                .get();


        int maxConnections = fogType.getMaximumConnections();


        while (uncoveredDevices(routers) != 0 && maxConnections > 0) {
            for (Router router : routers) {
                while (maxConnections > 0 && !router.covered()) {
                    router.incrementCoveredCount();
                    maxConnections--;
                }
            }
        }


        if (fogType != null) {
            return fogType;

        } else {
            Logger.getInstance().log("There is no suitable fog node type.");
            System.exit(1);
            return null;
        }

    }

    /**
     * Sum of uncoverered devices connected to a set of given routers.
     *
     * @param routers
     * @return
     */
    private int uncoveredDevices(Collection<Router> routers) {
        int uncoveredDevices = 0;
        for (Router router : routers) {
            if (router != null) {

                if (!router.covered()) {
                    uncoveredDevices += router.getUncoveredDevices();
                }
            }
        }
        return uncoveredDevices;
    }


    private int coveredDevices(Collection<Router> routers) {
        int coveredDevices = 0;
        for (Router router : routers) {
            if (router != null) {
                coveredDevices += router.coveredDevices();
            }
        }

        return coveredDevices;
    }


    private double calculateRatio(int connectedDevices, FogNodeType fogNodeType) {
        double ratio = (connectedDevices - fogNodeType.getMaximumConnections()) / fogNodeType.getCosts();
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
}
