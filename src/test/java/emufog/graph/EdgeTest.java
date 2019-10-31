/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdgeTest {

    private final AS defaultAS = new AS(1);

    private final Node from = new BackboneNode(1, defaultAS);

    private final Node to = new BackboneNode(2, defaultAS);

    private final Edge edge = new Edge(1, from, to, 1.3F, 10.5F);

    @Test
    void checkGetters() {
        assertEquals(10.5F, edge.getBandwidth());
        assertEquals(1.3F, edge.getDelay());
        assertEquals(from, edge.getSource());
        assertEquals(to, edge.getDestination());
    }

    @Test
    void checkIfEdgeIsAssigned() {
        assertEquals(1, from.edges.size());
        assertEquals(edge, from.edges.get(0));
        assertEquals(1, to.edges.size());
        assertEquals(edge, to.edges.get(0));
    }

    @Test
    void creationWithNoSource() {
        assertThrows(NullPointerException.class, () -> {
            new Edge(1, null, to, 1, 1);
        });
    }

    @Test
    void creationWithNoDestination() {
        assertThrows(NullPointerException.class, () -> {
            new Edge(1, from, null, 1, 1);
        });
    }

    @Test
    void checkForOtherEnd() {
        Node destination = edge.getDestinationForSource(from);
        assertEquals(to, destination);
        Node source = edge.getDestinationForSource(to);
        assertEquals(from, source);
    }

    @Test
    void checkForOtherEndNull() {
        Node destination = edge.getDestinationForSource(null);
        assertNull(destination);
    }

    @Test
    void checkForThirdNode() {
        Node third = new BackboneNode(3, null);
        Node destination = edge.getDestinationForSource(third);
        assertNull(destination);
    }

    @Test
    void checkForCrossASEdge() {
        assertFalse(edge.isCrossASEdge());
    }

    @Test
    void checkForCrossASEdgeNewAS() {
        Node node = new BackboneNode(3, new AS(3));
        Edge edge = new Edge(1, from, node, 1, 1);
        assertTrue(edge.isCrossASEdge());
    }
}