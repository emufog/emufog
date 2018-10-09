/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package emufog.backbone;

import emufog.graph.*;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.util.*;

/**
 * This worker class operates on a single AS of the graph so it can used in parallel.
 * Executes the 2nd and 3rd step of the classification algorithm.
 */
class BackboneWorker implements Runnable {

    /* percentage of the average degree to compare to */
    private static final float BACKBONE_DEGREE_PERCENTAGE = 0.6f;

    /* AS associated with this worker */
    private final AS as;

    /* converter to mark backbone nodes */
    private final SwitchConverter converter;

    /**
     * Creates a new worker instance to compute the backbone classification algorithms on a given AS.
     * Only converts the given as from the graph instance.
     *
     * @param as AS to operate on
     */
    BackboneWorker(AS as) {
        this.as = as;
        converter = new SwitchConverter();
    }

    @Override
    public void run() {
        Logger logger = Logger.getInstance();

        //2nd step
        long start = System.nanoTime();
        convertHighDegrees();
        long end = System.nanoTime();
        logger.log(as + " Step 2 - Time: " + Logger.convertToMs(start, end), LoggerLevel.ADVANCED);
        logger.log("Backbone Size: " + as.getSwitches().size(), LoggerLevel.ADVANCED);
        logger.log("Edge Size: " + as.getRouters().size(), LoggerLevel.ADVANCED);

        // 3rd step
        start = System.nanoTime();
        buildSingleBackbone();
        end = System.nanoTime();
        logger.log(as + " Step 3 - Time: " + Logger.convertToMs(start, end), LoggerLevel.ADVANCED);
        logger.log("Backbone Size: " + as.getSwitches().size(), LoggerLevel.ADVANCED);
        logger.log("Edge Size: " + as.getRouters().size(), LoggerLevel.ADVANCED);
    }

    /**
     * Converts nodes with an above average degree to a backbone node.
     */
    private void convertHighDegrees() {
        float averageDegree = calculateAverageDegree() * BACKBONE_DEGREE_PERCENTAGE;
        List<Router> toConvert = new ArrayList<>();

        for (Router router : as.getRouters()) {
            if (router.getDegree() >= averageDegree) {
                toConvert.add(router);
            }
        }

        for (Router r : toConvert) {
            converter.convert(r);
        }
    }

    /**
     * Creates a single connected backbone by using the Breadth-First-Algorithm.
     */
    private void buildSingleBackbone() {
        // bit sets to check for visited nodes and nodes in the queue
        BitSet visited = new BitSet();
        BitSet seen = new BitSet();
        Queue<Node> queue = new ArrayDeque<>();
        // map nodes to their respective predecessors
        Map<Node, Node> predecessors = new HashMap<>();

        // start with any backbone node
        Node current = as.getSwitches().iterator().next();
        if (current == null) {
            return;
        }

        predecessors.put(current, current);

        while (current != null) {
            visited.set(current.getID());

            // follow a trace via the predecessor to convert all on this way
            if (current instanceof Switch && predecessors.get(current) instanceof Router) {
                Node predecessor = predecessors.get(current);
                while (predecessor instanceof Router) {
                    converter.convert(predecessor);

                    predecessor = predecessors.get(predecessor);
                }
            }

            // add or update neighborhood
            for (Edge e : current.getEdges()) {
                if (!e.isCrossASEdge()) {
                    Node neighbor = e.getDestinationForSource(current);
                    // avoid visiting twice
                    if (!visited.get(neighbor.getID())) {
                        if (seen.get(neighbor.getID())) {
                            // update the predecessor if necessary
                            if (current instanceof Switch && predecessors.get(neighbor) instanceof Router) {
                                predecessors.put(neighbor, current);
                            }
                        } else {
                            // push a new node to the queue
                            predecessors.put(neighbor, current);
                            queue.add(neighbor);
                            seen.set(neighbor.getID());
                        }
                    }
                }
            }

            current = queue.poll();
        }
    }

    /**
     * Returns the average degree of the autonomous system based on the router and switch nodes.
     *
     * @return the average degree
     */
    private float calculateAverageDegree() {
        int sum = 0;

        for (Node n : as.getRouters()) {
            sum += n.getDegree();
        }
        for (Node n : as.getSwitches()) {
            sum += n.getDegree();
        }

        return sum / (as.getSwitches().size() + as.getRouters().size());
    }
}
