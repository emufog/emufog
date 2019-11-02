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
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackboneNodeTest {

    private final AS defaultAS = new AS(1);

    private final EmulationSettings defaultSettings = new EmulationSettings("1.2.3.4", null);

    private final BackboneNode node = new BackboneNode(1, defaultAS);

    @Test
    void testToString() {
        assertEquals("s1", node.toString());
    }

    @Test
    void testInit() {
        assertEquals(1, node.getID());
        assertEquals(defaultAS, node.getAS());
        assertEquals(0, node.getEdges().size());
        assertNull(node.getEmulationNode());
    }

    @Test
    void testDegree() {
        BackboneNode testNode1 = new BackboneNode(1, defaultAS);
        BackboneNode testNode2 = new BackboneNode(2, defaultAS);
        assertEquals(0, testNode1.getEdges().size());
        assertEquals(0, testNode1.getDegree());
        Edge edge = new Edge(1, testNode1, testNode2, 1, 1);
        assertEquals(1, testNode1.getEdges().size());
        assertEquals(1, testNode1.getDegree());
    }

    @Test
    void testCopyFromOldNode() {
        BackboneNode testNode1 = new BackboneNode(1, defaultAS);
        BackboneNode testNode2 = new BackboneNode(2, defaultAS);
        BackboneNode testNode3 = new BackboneNode(3, defaultAS);
        Edge edge1 = new Edge(1, testNode1, testNode2, 1, 1);
        assertEquals(1, testNode1.getDegree());
        assertEquals(edge1, testNode1.getEdges().get(0));
        assertEquals(testNode2, testNode1.getEdges().get(0).getDestination());
        assertEquals(0, testNode3.getDegree());
        testNode3.copyFromOldNode(testNode1);
        assertEquals(1, testNode3.getDegree());
        assertEquals(edge1, testNode3.getEdges().get(0));
        assertEquals(testNode2, testNode3.getEdges().get(0).getDestination());
    }

    @Test
    void testEmulationSettings() {
        BackboneNode testNode1 = new BackboneNode(1, defaultAS);
        assertFalse(testNode1.hasEmulationSettings());
        testNode1.emulationSettings = defaultSettings;
        assertTrue(testNode1.hasEmulationSettings());
        assertEquals(defaultSettings, testNode1.getEmulationNode());
    }

    @Test
    void testConvertToBackbone() {
        compareTwoNodes(node, node.convertToBackboneNode());
    }

    private void compareTwoNodes(Node expected, Node actual) {
        assertEquals(expected, actual);
        assertEquals(expected.id, actual.id);
        assertEquals(expected.as, actual.as);
        assertEquals(expected.getDegree(), actual.getDegree());
        for (Edge e : expected.edges) {
            assertTrue(actual.edges.contains(e));
        }
        assertEquals(expected.emulationSettings, actual.emulationSettings);
    }

    @Test
    void testConvertToEdgeNode() {
        BackboneNode testNode1 = new BackboneNode(1, defaultAS);
        assertEquals(testNode1, testNode1.convertToEdgeNode());
    }

    @Test
    void testConvertToEdgeDeviceNode() {
        BackboneNode testNode1 = new BackboneNode(1, defaultAS);
        assertEquals(testNode1, testNode1.convertToEdgeDeviceNode(defaultSettings));
    }
}