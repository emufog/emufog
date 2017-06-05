package graph.emufog;

/**
 * Interface to calculate the latency of an edge.
 */
public interface ILatencyCalculator {

    /**
     * Calculates the latency for an edge between two endpoints in a 2D plane.
     *
     * @param x1 x coordinate of 1st endpoint
     * @param y1 y coordinate of 1st endpoint
     * @param x2 x coordinate of 2nd endpoint
     * @param y2 y coordinate of 2nd endpoint
     * @return the calculated latency
     */
    float getLatency(float x1, float y1, float x2, float y2);
}
