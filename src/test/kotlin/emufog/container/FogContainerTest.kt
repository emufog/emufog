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
package emufog.container

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FogContainerTest {

    private val defaultContainer = FogContainer("name", "tag", 1024, 1F, 1, 5.5F)

    @Test
    fun `test the default init`() {
        assertEquals("name", defaultContainer.name)
        assertEquals("tag", defaultContainer.tag)
        assertEquals(1024, defaultContainer.memoryLimit)
        assertEquals(1F, defaultContainer.cpuShare)
        assertEquals(1, defaultContainer.maxClients)
        assertEquals(5.5F, defaultContainer.costs)
    }

    @Test
    fun `fullName should return the correct concatenation`() {
        assertEquals("name:tag", defaultContainer.fullName())
    }

    @Test
    fun `empty name should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FogContainer("", "tag", 1024, 1F, 1, 5.5F)
        }
    }

    @Test
    fun `empty tag should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FogContainer("name", "", 1024, 1F, 1, 5.5F)
        }
    }

    @Test
    fun `negative memory should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FogContainer("name", "tag", -1024, 1F, 1, 5.5F)
        }
    }

    @Test
    fun `negative cpu share should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FogContainer("name", "tag", 1024, -1F, 1, 5.5F)
        }
    }

    @Test
    fun `user capacity of zero or less should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FogContainer("name", "tag", 1024, 1F, 0, 5.5F)
        }
    }

    @Test
    fun `deployment costs of less than zero should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FogContainer("name", "tag", 1024, 1F, 1, -1F)
        }
    }
}