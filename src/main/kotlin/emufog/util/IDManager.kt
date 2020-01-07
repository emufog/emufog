/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
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

import java.util.BitSet

/**
 * The ID manager keeps track of all IDs in use and calculates based on that data the next available ID such that all
 * IDs are unique.
 */
class IDManager {

    /**
     * the current ID is the last one in use
     */
    private var current: Int = 0

    /**
     * set to keep track of all used IDs
     */
    private val bitSet: BitSet = BitSet()

    /**
     * Calculates and returns the next available unique ID. The ID is not marked as used.
     */
    fun getNextID(): Int = bitSet.nextClearBit(current)

    /**
     * Marks an ID as used so it cannot be assigned to another object.
     *
     * @param id the ID already in use
     */
    fun setUsed(id: Int) {
        bitSet[id] = true
    }

    /**
     * Checks if the given ID is already in use.
     *
     * @param id ID to check
     * @return `true` if the ID already used, `false` otherwise
     */
    fun isUsed(id: Int): Boolean = bitSet[id]
}
