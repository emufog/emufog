package graph.emufog;

/**
 * Coordinate structure to map nodes to their respective coordinates on the 2D plane.
 */
class NodeCoordinates {

    /* x coordinate */
    final float x;

    /* y coordinate */
    final float y;

    /**
     * Creates a new coordinate structure
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    NodeCoordinates(float x, float y) {
        this.x = x;
        this.y = y;
    }
}