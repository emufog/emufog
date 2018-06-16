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

    private float delayBoundary = getSettings().getDelayBoundary();

    @Override
    public void identifyFogNodes(MutableNetwork topology) {

        //get all edge routers that have connected devices.
        topology.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(EDGE_ROUTER) && ((Router) n).hasDevices())
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

    /**
     * Iterate over given list of edge Routers and find all suitable backbone routers using dijkstra to find shortest
     * paths.
     */
    private void determineCandidateRouters() {

        for (Router edgeRouter : edgeRouters) {

            //start Dijkstra for current edgeRouter
            calculateShortestPathFromEdgeRouter(edgeRouter);

            /*
            Iterate over all backbone router in given topology and filter according to defined delay and hop
            threshold.
             */
            for (Iterator<Node> it = getTopology().nodes().stream().filter(n -> n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER)).iterator(); it.hasNext(); ) {
                Router backboneRouter = (Router) it.next();

                if (isValidBackboneRouter(backboneRouter)) {
                    addBackboneRouter(backboneRouter, edgeRouter);
                    resetNode(backboneRouter);
                } else {
                    resetNode(backboneRouter);
                }
            }
        }
    }

    /**
     * Checks whether given router fulfills configured delay and threshold configuration. Adds edge router only
     * iff requirements are satisfied.
     *
     * @param router
     * @return
     */
    private boolean isValidBackboneRouter(Router router) {
        if (!router.getShortestPath().isEmpty() && router.getShortestPath().size() <= threshold) {
            if (router.getDistance() <= delayBoundary) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reset node set distance to max integer and clear shortest path list.
     *
     * @param node
     */
    static void resetNode(Node node) {
        node.setDistance((float) Integer.MAX_VALUE);
        List<Node> shortestPath = new LinkedList<>();
        node.setShortestPath(shortestPath);
    }

    /**
     * Calculate shortest path from given edge router to each backbone router in topology by using dijkstra's algorithm.
     *
     * @param edgeRouter
     */
    static void calculateShortestPathFromEdgeRouter(Router edgeRouter) {

        edgeRouter.setDistance(0f);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        unsettledNodes.add(edgeRouter);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);

            for (Node node : getTopology().adjacentNodes(currentNode)) {
                if (node instanceof Router && ((Router) node).getType().equals(BACKBONE_ROUTER)) {
                    Node adjacentNode = (Node) node;
                    Link link = getTopology().edgeConnectingOrNull(adjacentNode, currentNode);
                    float delay = link.getDelay();

                    if (!settledNodes.contains(adjacentNode)) {
                        calculateMinimumDistance(adjacentNode, delay, currentNode);
                        unsettledNodes.add(adjacentNode);
                    }
                }
            }

            settledNodes.add(currentNode);
        }

    }

    static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
        Node lowestDistanceNode = null;
        float lowestDistance = Integer.MAX_VALUE;
        for (Node node : unsettledNodes) {
            float nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(Node evaluationNode, float delay, Node sourceNode) {
        float sourceDistance = sourceNode.getDistance();
        if (sourceDistance + delay < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + delay);
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
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
     * Find one suitable fog node type and adapt covered devices count in topology.
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
     * Sum of uncovered devices connected to a set of given routers.
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

    private double calculateRatio(int connectedDevices, FogNodeType fogNodeType) {
        double ratio = (connectedDevices - fogNodeType.getMaximumConnections()) / fogNodeType.getCosts();
        logger.log("Connected Devices: " + connectedDevices + "\n"
                + "Costs: " + fogNodeType.getCosts()
                + "\n" + "Max connections: " + fogNodeType.getMaximumConnections() + "\n"
                + "Ratio: " + ratio + "\n");
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
