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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TupleTest {

    @Test
    fun `test hashCode with Int`() {
        val tuple = Tuple(42, "hello world")
        assertEquals(42, tuple.hashCode())
    }

    @Test
    fun `test hashCode with String`() {
        val s = "hello world"
        val tuple = Tuple(s, 42)
        assertEquals(s.hashCode(), tuple.hashCode())
    }

    @Test
    fun `test equals with Int`() {
        val tuple1 = Tuple(42, "hello world")
        val tuple2 = Tuple(42, "hello world2")
        assertTrue(tuple1 == tuple2)
        assertFalse(tuple1 === tuple2)
    }

    @Test
    fun `test equals should fail with Int`() {
        val tuple1 = Tuple(42, "hello world")
        val tuple2 = Tuple(43, "hello world2")
        assertFalse(tuple1 == tuple2)
        assertFalse(tuple1 === tuple2)
    }

    @Test
    fun `test equals with String`() {
        val tuple1 = Tuple("hello world", 42.1F)
        val tuple2 = Tuple("hello world", 42)
        assertTrue(tuple1 == tuple2)
        assertFalse(tuple1 === tuple2)
    }

    @Test
    fun `test equals should fail with String`() {
        val tuple1 = Tuple("hello world", 42.1F)
        val tuple2 = Tuple("hello World", 42)
        assertFalse(tuple1 == tuple2)
        assertFalse(tuple1 === tuple2)
    }
}