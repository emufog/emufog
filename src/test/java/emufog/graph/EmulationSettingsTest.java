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

import emufog.container.Container;
import emufog.container.DeviceContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmulationSettingsTest {

    @Test
    void testNullInit() {
        EmulationSettings settings = new EmulationSettings(null, null);
        assertNull(settings.getIP());
        assertNull(settings.getContainer());
    }

    @Test
    void testEmptyIP() {
        EmulationSettings settings = new EmulationSettings("", null);
        assertEquals("", settings.getIP());
    }

    @Test
    void testInit() {
        Container container = new DeviceContainer("name", "latest", 1024, 2.4F, 1, 1.4F);
        EmulationSettings settings = new EmulationSettings("1.2.3.4", container);
        assertEquals("1.2.3.4", settings.getIP());
        Container actual = settings.getContainer();
        assertEquals("name", actual.getName());
        assertEquals(1024, actual.getMemoryLimit());
        assertEquals(2.4F, actual.getCpuShare());
    }
}