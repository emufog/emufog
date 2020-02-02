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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import emufog.container.DeviceContainer
import emufog.container.FogContainer
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * Top level configuration of EmuFog based on the given .yaml file.
 *
 * @property baseAddress base address of the logical network to start with
 * @property overWriteOutputFile `true` if the experiment file should be overwritten
 * @property maxFogNodes maximum number of fog nodes that can be placed in the topology
 * @property costThreshold the threshold of the cost function to limit the placement
 * @property hostDeviceLatency latency to use to connect edge devices to the edge
 * @property hostDeviceBandwidth bandwidth to use to connect edge devices to the edge
 * @property deviceNodeTypes list of possible device containers representing an edge device
 * @property fogNodeTypes list of possible fog node types that can be placed in the topology
 */
class Config internal constructor(
    @JsonProperty("base-address") val baseAddress: String,
    @JsonProperty("overwrite-experiment-file") val overWriteOutputFile: Boolean,
    @JsonProperty("max-fog-nodes") val maxFogNodes: Int,
    @JsonProperty("cost-threshold") val costThreshold: Float,
    @JsonProperty("host-device-latency") val hostDeviceLatency: Float,
    @JsonProperty("host-device-bandwidth") val hostDeviceBandwidth: Float,
    @JsonProperty("device-node-types") deviceNodeTypes: Collection<DeviceTypeConfig>,
    @JsonProperty("fog-node-types") fogNodeTypes: Collection<FogTypeConfig>
) {

    val deviceNodeTypes: List<DeviceContainer> = deviceNodeTypes.map { it.toDeviceContainer() }

    val fogNodeTypes: List<FogContainer> = fogNodeTypes.map { it.toFogContainer() }
}

/**
 * Reads in and returns the configuration from the given [path] variable. The path needs to point to a .yaml file.
 *
 * @param path path to the config .yaml file
 * @return read in config instance
 * @throws IOException thrown if the read in fails
 * @throws IllegalArgumentException if the given path is not a .yaml file
 */
fun readConfig(path: Path): Config {
    val matcher = FileSystems.getDefault().getPathMatcher("glob:**.yaml")
    require(matcher.matches(path)) { "The file ending does not match .yaml." }

    // parse YAML document to a kotlin object
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    return mapper.readValue(path.toFile(), Config::class.java)
        ?: throw IOException("Failed to parse the YAML file: $path")
}

/**
 * Maps the configuration for a fog type to an instance of the model.
 */
private fun FogTypeConfig.toFogContainer(): FogContainer {
    return FogContainer(
        this.containerImage.name,
        this.containerImage.version ?: "latest",
        this.memoryLimit,
        this.cpuShare,
        this.maximumConnections,
        this.costs
    )
}

/**
 * Maps the configuration for a device type to an instance of the model.
 */
private fun DeviceTypeConfig.toDeviceContainer(): DeviceContainer {
    return DeviceContainer(
        this.containerImage.name,
        this.containerImage.version ?: "latest",
        this.memoryLimit,
        this.cpuShare,
        this.scalingFactor ?: 1,
        this.averageDeviceCount
    )
}