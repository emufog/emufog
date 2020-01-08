/*
 * MIT License
 *
 * Copyright (c) 2020 emufog contributors
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
package emufog.graph

import emufog.config.Config
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class IPManagerTest {

    @Test
    fun `should fail if IP format is too short`() {
        val config = mockk<Config> {
            every { baseAddress } returns "254.254.254"
        }
        assertThrows<IllegalArgumentException> {
            IPManager(config)
        }
    }

    @Test
    fun `should fail if IP format is no number`() {
        val config = mockk<Config> {
            every { baseAddress } returns "254.254.254.2A"
        }
        assertThrows<NumberFormatException> {
            IPManager(config)
        }
    }

    @Test
    fun `get next IP for 0 0 0 0`() {
        val config = mockk<Config> {
            every { baseAddress } returns "0.0.0.0"
        }
        val manager = IPManager(config)
        assertEquals("0.0.0.1", manager.nextIPV4Address())
    }

    @Test
    fun `get next IP for 0 0 0 253`() {
        val config = mockk<Config> {
            every { baseAddress } returns "0.0.0.253"
        }
        val manager = IPManager(config)
        assertEquals("0.0.0.254", manager.nextIPV4Address())
        assertEquals("0.0.1.0", manager.nextIPV4Address())
    }

    @Test
    fun `get next IP for 2 254 254 254`() {
        val config = mockk<Config> {
            every { baseAddress } returns "2.254.254.254"
        }
        val manager = IPManager(config)
        assertEquals("3.0.0.0", manager.nextIPV4Address())
    }

    @Test
    fun `should fail after 254 254 254 254`() {
        val config = mockk<Config> {
            every { baseAddress } returns "254.254.254.254"
        }
        val manager = IPManager(config)
        assertThrows<IllegalStateException> {
            manager.nextIPV4Address()
        }
    }
}