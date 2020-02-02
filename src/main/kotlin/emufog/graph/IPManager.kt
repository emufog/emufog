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

/**
 * The IP manager calculates IP address within the subnet address space defined in the base address of the given
 * config. Keeps track of the last recent IP.
 * @throws IllegalArgumentException if the given address is in the wrong format
 */
internal class IPManager(startAddress: String) {

    private var byte1: Int

    private var byte2: Int

    private var byte3: Int

    private var byte4: Int

    init {
        val splits = startAddress.split(".")
        require(splits.size == 4) { "The format of the IP is invalid." }
        byte1 = splits[3].toInt()
        byte2 = splits[2].toInt()
        byte3 = splits[1].toInt()
        byte4 = splits[0].toInt()
    }

    /**
     * Calculates and returns the next available IP address in the subnet.
     * @throws IllegalStateException if the the ip exceeds 254.254.254.254
     */
    fun nextIPV4Address(): String {
        if (byte1 != 254) {
            byte1++

            return getString()
        }
        byte1 = 0

        if (byte2 != 254) {
            byte2++

            return getString()
        }
        byte2 = 0

        if (byte3 != 254) {
            byte3++

            return getString()
        }
        byte3 = 0

        if (byte4 != 254) {
            byte4++

            return getString()
        }

        throw IllegalStateException("There is no next IPv4 after 254.254.254.254")
    }

    private fun getString() = String.format("%d.%d.%d.%d", byte4, byte3, byte2, byte1)
}