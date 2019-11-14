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
 * This fog container image represents a fog computing node in the topology. It can serve a fixed
 * number of clients and is associated with deployment costs
 *
 * @property name name of container image to deploy
 * @property tag version tag of container image to deploy
 * @property memoryLimit upper limit of memory to use in Bytes
 * @property cpuShare share of the sum of available computing resources
 * @property maxClients maximum number of clients this container can serve
 * @property costs costs to deploy this container in the topology
 */
data class FogContainer(
    override val name: String,
    override val tag: String,
    override val memoryLimit: Int,
    override val cpuShare: Float,
    val maxClients: Int,
    val costs: Float
) : Container
