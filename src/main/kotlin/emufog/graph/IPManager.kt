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
package emufog.graph

import emufog.config.Config

/**
 * The IP manager calculates IP address within the subnet address space defined in the base address of the given
 * config. Keeps track of the last recent IP.
 */
internal class IPManager(config: Config) {

    /**
     * the last assigned IP in the network
     */
    private var lastIP: String = config.baseAddress

    /**
     * Calculates and returns the next available IP address in the subnet.
     */
    fun nextIPV4Address(): String {
        val nums = lastIP.split(".")
        var i: Int = (nums[0].toInt() shl 24 or (nums[2].toInt() shl 8) or (nums[1].toInt() shl 16) or nums[3].toInt())
        +1

        // If you wish to skip over .255 addresses.
        if (i.toByte() == (-1).toByte()) {
            i++
        }

        lastIP = String.format("%d.%d.%d.%d", i ushr 24 and 0xFF, i shr 16 and 0xFF, i shr 8 and 0xFF, i and 0xFF)

        return lastIP
    }
}