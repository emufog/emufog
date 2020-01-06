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
package emufog.container

/**
 * This fog container image represents a fog computing node in the topology. It can serve a fixed number of clients and
 * is associated with deployment costs.
 *
 * @property maxClients maximum number of clients this container can serve `>= 1`
 * @property costs costs to deploy this container in the topology `>= 0`
 * @throws IllegalArgumentException thrown if [memoryLimit] is `< 0`, [cpuShare] is `< 0`, [name] or [tag] are blank,
 *  [maxClients] is `< 1`, [costs] is `< 0`
 */
class FogContainer(
    name: String,
    tag: String,
    memoryLimit: Int,
    cpuShare: Float,
    val maxClients: Int,
    val costs: Float
) : Container(name, tag, memoryLimit, cpuShare) {

    init {
        require(maxClients >= 1) { "The fog container's user capacity has to be greater equal than 1." }
        require(costs >= 0) { "The fog container's deployment costs have to be positive." }
    }
}
