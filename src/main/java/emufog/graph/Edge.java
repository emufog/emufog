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
package emufog.graph;

/**
 * This class represents an edge in the network graph. The connection between the two nodes is bidirectional.
 * Latency and bandwidth apply to both directions.
 */
public class Edge {

    /**
     * unique identifier of the edge object
     */
    private final int id;

    /**
     * latency delay on this edge in ms
     */
    private final float delay;

    /**
     * bandwidth of the connection on Mbit/s
     */
    private final float bandwidth;

    /**
     * one end of the connection
     */
    private Node from;

    /**
     * the other end of the connection
     */
    private Node to;

    /**
     * Creates a new edge instance connecting two nodes of the graph.
     * The connection is based on the delay and the bandwidth given.
     *
     * @param id        unique identifier
     * @param from      starting node
     * @param to        ending node
     * @param delay     latency delay in ms
     * @param bandwidth bandwidth in Mbit/s
     */
    Edge(int id, Node from, Node to, float delay, float bandwidth) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.delay = delay;
        this.bandwidth = bandwidth;

        from.addEdge(this);
        to.addEdge(this);
    }

    /**
     * Returns the bandwidth limitation of the connection in Mbit/s.
     *
     * @return maximal bandwidth
     */
    public float getBandwidth() {
        return bandwidth;
    }

    /**
     * Returns the latency delay of the connection in ms.
     *
     * @return latency of the connection
     */
    public float getDelay() {
        return delay;
    }

    /**
     * Returns the source/first node of the connection.
     *
     * @return source/first node
     */
    public Node getSource() {
        return from;
    }

    /**
     * Returns the destination/second node of the connection.
     *
     * @return destination/second node
     */
    public Node getDestination() {
        return to;
    }

    /**
     * Returns the other end of the connection for the given node.
     * In case the node is not part of the connection the method returns {@code null}.
     *
     * @param node node to find the partner for
     * @return the other end of the connection or {@code null} if node is not part of this edge
     */
    public Node getDestinationForSource(Node node) {
        if (from.equals(node)) {
            return to;
        }
        if (to.equals(node)) {
            return from;
        }

        return null;
    }

    /**
     * Indicates whether this edge connects two different ASs or not.
     *
     * @return true if edge is connecting different ASs, false otherwise
     */
    public boolean isCrossASEdge() {
        return !from.as.equals(to.as);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Edge)) {
            return false;
        }

        Edge other = (Edge) o;

        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Edge: " + id;
    }

    /**
     * Sets the first/source node of the connection.
     *
     * @param source first/source node to set
     */
    void setSource(Node source) {
        from = source;
    }

    /**
     * Sets the second/destination node of the connection.
     *
     * @param destination second/destination node to set
     */
    void setDestination(Node destination) {
        to = destination;
    }
}
