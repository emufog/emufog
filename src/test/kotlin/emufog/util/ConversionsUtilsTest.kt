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
package emufog.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConversionsUtilsTest {

    @Test
    fun `format long interval zero based start`() {
        val s = ConversionsUtils.formatTimeInterval(0, 4900000000000L)
        assertEquals("1h 21min 40s 0ms", s)
    }

    @Test
    fun `format long interval non zero based start`() {
        val s = ConversionsUtils.formatTimeInterval(42, 4900000000042L)
        assertEquals("1h 21min 40s 0ms", s)
    }

    @Test
    fun `format leading min`() {
        val s = ConversionsUtils.formatTimeInterval(0, 2520000000000L)
        assertEquals("42min 0s 0ms", s)
    }

    @Test
    fun `format leading sec`() {
        val s = ConversionsUtils.formatTimeInterval(0, 5000000000L)
        assertEquals("5s 0ms", s)
    }

    @Test
    fun `format leading milliseconds`() {
        val s = ConversionsUtils.formatTimeInterval(0, 124000000L)
        assertEquals("124ms", s)
    }

    @Test
    fun `format empty interval`() {
        val s = ConversionsUtils.formatTimeInterval(0, 0)
        assertEquals("0ms", s)
    }

    @Test
    fun `format below 1 ms`() {
        val s = ConversionsUtils.formatTimeInterval(0, 999)
        assertEquals("0ms", s)
    }

    @Test
    fun `format negative interval`() {
        val s = ConversionsUtils.formatTimeInterval(123456789, 1)
        assertEquals("0ms", s)
    }
}