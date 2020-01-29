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
 * Abstract class of a container that can be deployed in the final experiment. Consists of a name and tag as well as
 * limits on memory and cpu share.
 *
 * @property name name of container image to deploy
 * @property tag version tag of container image to deploy
 * @property memoryLimit upper limit of memory to use in Bytes `>= 0`
 * @property cpuShare share of the sum of available computing resources `>= 0`
 * @throws IllegalArgumentException thrown if [memoryLimit] is `< 0`, [cpuShare] is `< 0`, [name] or [tag] are blank
 */
abstract class Container(
    val name: String,
    val tag: String,
    val memoryLimit: Int,
    val cpuShare: Float
) {

    init {
        require(name.isNotBlank()) { "The container's name can not be blank." }
        require(tag.isNotBlank()) { "The container's tag can not be blank." }
        require(memoryLimit >= 0) { "The container's memory limit can not be negative." }
        require(cpuShare >= 0) { "The container's cpu share can not be negative." }
    }

    /**
     * Returns the full name of the container in the form of name:tag.
     */
    fun fullName(): String = "$name:$tag"

    override fun toString(): String = fullName()
}
