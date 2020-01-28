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
package emufog.fog

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FogResultTest {

    @Test
    fun `initialize a new instance`() {
        val result = FogResult()
        assertFalse(result.status)
        assertEquals(0, result.placements.size)
    }

    @Test
    fun `set the status to true`() {
        val result = FogResult()
        assertFalse(result.status)
        result.setSuccess()
        assertTrue(result.status)
    }

    @Test
    fun `set the status to false`() {
        val result = FogResult()
        assertFalse(result.status)
        result.setFailure()
        assertFalse(result.status)
        result.setSuccess()
        assertTrue(result.status)
        result.setFailure()
        assertFalse(result.status)
    }

    @Test
    fun `addPlacement should increase list by one`() {
        val result = FogResult()
        assertEquals(0, result.placements.size)
        val placement: FogNodePlacement = mockk()

        result.addPlacement(placement)

        assertEquals(1, result.placements.size)
        assertEquals(placement, result.placements[0])
    }

    @Test
    fun `addPlacements should increase list by all elements of the collection`() {
        val result = FogResult()
        assertEquals(0, result.placements.size)
        val placement1: FogNodePlacement = mockk()
        val placement2: FogNodePlacement = mockk()

        result.addPlacements(listOf(placement1, placement2))

        assertEquals(2, result.placements.size)
        assertTrue(result.placements.contains(placement1))
        assertTrue(result.placements.contains(placement2))
    }
}