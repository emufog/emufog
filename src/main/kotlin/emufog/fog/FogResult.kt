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

/**
 * This class represents the outcome of a fog node placement algorithm. If `true` the [placements] is returning the
 * correct result. For `false` [placements] can be any intermediate result.
 *
 * @property status represents the success or failure of the placement
 * @property placements the individual fog node placements.
 */
class FogResult internal constructor() {

    var status: Boolean = false
        private set

    /**
     * list of all fog node placements in the result set, exposed via [placements]
     */
    private val results: MutableList<FogNodePlacement> = ArrayList()

    val placements: List<FogNodePlacement>
        get() = results


    /**
     * Adds a new fog node placement to the list of the result object.
     *
     * @param placement fog node placement to add
     */
    internal fun addPlacement(placement: FogNodePlacement) {
        results.add(placement)
    }

    /**
     * Adds a collection of fog node placements to the list of the result object.
     *
     * @param placements collection of fog node placements to add
     */
    internal fun addPlacements(placements: Collection<FogNodePlacement>) {
        results.addAll(placements)
    }

    /**
     * Sets the [.status] of the result object to `true`.
     */
    internal fun setSuccess() {
        status = true
    }

    /**
     * Sets the [.status] of the result object to `false`.
     */
    internal fun setFailure() {
        status = false
    }
}
