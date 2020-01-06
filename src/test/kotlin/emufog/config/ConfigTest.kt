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
package emufog.config

import emufog.container.Container
import emufog.container.DeviceContainer
import emufog.container.FogContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths

internal class ConfigTest {

    private val resourcesDir = Paths.get("src", "test", "resources", "config")

    @Test
    fun `test a not yaml file`() {
        assertThrows<IllegalArgumentException> {
            Config.updateConfig(Paths.get("file.txt"))
        }
    }

    @Test
    fun `test a directory`() {
        assertThrows<IllegalArgumentException> {
            Config.updateConfig(Paths.get("."))
        }
    }

    @Test
    fun `test a not existing file`() {
        assertThrows<FileNotFoundException> {
            Config.updateConfig(Paths.get("not-existing.yaml"))
        }
    }

    @Test
    fun `test the constructor logic`() {
        val baseAddress = "1.2.3.4"
        val overwrite = true
        val maxNodes = 100
        val threshold = 123.45F
        val hostLatency = 1.7F
        val hostBandwidth = 5F
        val devices = listOf(
            DeviceTypeConfig(ContainerNameConfig("device", "tag"), 1024, 1F, 1, 1.5F),
            DeviceTypeConfig(ContainerNameConfig("device2", null), 0, 0F, 2, 1.5F)
        )
        val fogNodes = listOf(FogTypeConfig(ContainerNameConfig("fog", "latest"), 10, 1F, 2, 1F))

        val config = Config(baseAddress, overwrite, maxNodes, threshold, hostLatency, hostBandwidth, devices, fogNodes)

        assertEquals(baseAddress, config.baseAddress)
        assertEquals(overwrite, config.overWriteOutputFile)
        assertEquals(maxNodes, config.maxFogNodes)
        assertEquals(threshold, config.costThreshold)
        assertEquals(hostLatency, config.hostDeviceLatency)
        assertEquals(hostBandwidth, config.hostDeviceBandwidth)

        assertEquals(2, config.deviceNodeTypes.size)
        assertTrue(equals(devices[0], config.deviceNodeTypes[0]))
        assertTrue(equals(devices[1], config.deviceNodeTypes[1]))

        assertEquals(1, config.fogNodeTypes.size)
        assertTrue(equals(fogNodes[0], config.fogNodeTypes[0]))
    }

    @Test
    fun `read in a valid config`() {
        Config.updateConfig(resourcesDir.resolve("config.yaml"))
        val config = Config.config
        requireNotNull(config)

        assertEquals("10.0.0.0", config.baseAddress)
        assertEquals(true, config.overWriteOutputFile)
        assertEquals(100, config.maxFogNodes)
        assertEquals(6F, config.costThreshold)
        assertEquals(0F, config.hostDeviceLatency)
        assertEquals(1000F, config.hostDeviceBandwidth)

        assertEquals(1, config.deviceNodeTypes.size)
        val deviceType = DeviceTypeConfig(ContainerNameConfig("ubuntu", "latest"), 524288000, 1F, 1, 1F)
        assertTrue(equals(deviceType, config.deviceNodeTypes[0]))

        assertEquals(2, config.fogNodeTypes.size)
        val fogType0 = FogTypeConfig(ContainerNameConfig("ubuntu", "trusty"), 1048576000, 1F, 1, 1F)
        assertTrue(equals(fogType0, config.fogNodeTypes[0]))
        val fogType1 = FogTypeConfig(ContainerNameConfig("debian", null), 1048576000, 1.5F, 5, 2.5F)
        assertTrue(equals(fogType1, config.fogNodeTypes[1]))
    }

    @Test
    fun `config with float instead of int should fail`() {
        assertThrows<IOException> {
            Config.updateConfig(resourcesDir.resolve("config_int_float.yaml"))
        }
    }

    @Test
    fun `config with missing latency should fail`() {
        assertThrows<IOException> {
            Config.updateConfig(resourcesDir.resolve("config_missing_latency.yaml"))
        }
    }

    @Test
    fun `config with wrong string for overwrite should fail`() {
        assertThrows<IOException> {
            Config.updateConfig(resourcesDir.resolve("config_overwrite_different_string.yaml"))
        }
    }

    private fun equalsBase(expected: ContainerTypeConfig, actual: Container): Boolean {
        return expected.containerImage.name == actual.name &&
            expected.containerImage.version ?: "latest" == actual.tag &&
            expected.memoryLimit == actual.memoryLimit &&
            expected.cpuShare == actual.cpuShare
    }

    private fun equals(expected: DeviceTypeConfig, actual: DeviceContainer): Boolean {
        return equalsBase(expected, actual) &&
            expected.scalingFactor == actual.scalingFactor &&
            expected.averageDeviceCount == actual.averageDeviceCount
    }

    private fun equals(expected: FogTypeConfig, actual: FogContainer): Boolean {
        return equalsBase(expected, actual) &&
            expected.maximumConnections == actual.maxClients &&
            expected.costs == actual.costs
    }
}