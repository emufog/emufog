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
import emufog.container.DeviceType
import emufog.container.FogType
import emufog.util.StringUtils.nullOrEmpty
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * Top level emufog.config object of the YAML document.
 */
class Config internal constructor(
    @JvmField @JsonProperty("base-address") val baseAddress: String,
    @JvmField @JsonProperty("overwrite-experiment-file") val overWriteOutputFile: Boolean,
    @JvmField @JsonProperty("max-fog-nodes") val maxFogNodes: Int,
    @JvmField @JsonProperty("cost-threshold") val costThreshold: Float,
    @JvmField @JsonProperty("host-device-latency") val hostDeviceLatency: Float,
    @JvmField @JsonProperty("host-device-bandwidth") val hostDeviceBandwidth: Float,
    @JsonProperty("device-node-types") deviceNodeTypes: Collection<DeviceTypeConfig>,
    @JsonProperty("fog-node-types") fogNodeTypes: Collection<FogTypeConfig>
) {

    @JvmField
    val deviceNodeTypes: List<DeviceType>

    @JvmField
    val fogNodeTypes: List<FogType>

    init {
        this.deviceNodeTypes = deviceNodeTypes.map { mapDeviceType(it) }
        this.fogNodeTypes = fogNodeTypes.map { mapFogType(it) }
    }

    companion object {

        /**
         * Returns the currently active configuration object.
         *
         * @return current configuration
         */
        var config: Config? = null
            private set

        @Throws(IOException::class)
        fun updateConfig(path: Path) {
            val matcher = FileSystems.getDefault().getPathMatcher("glob:**.yaml")
            require(matcher.matches(path)) { "The file ending does not match .yaml." }

            // parse YAML document to a java object
            val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
            val config = mapper.readValue(path.toFile(), Config::class.java)
                ?: throw IOException("Failed to parse the YAML file: $path")
            this.config = config
        }

        /**
         * Maps the configuration for a fog type to an instance of the model.
         *
         * @param f fog type emufog.config to map to an object
         * @return mapped fog type object
         */
        private fun mapFogType(f: FogTypeConfig): FogType {
            return if (nullOrEmpty(f.containerImage.version)) {
                FogType(f.containerImage.name, f.maximumConnections, f.costs, f.memoryLimit, f.cpuShare)
            } else {
                FogType(f.containerImage.name,
                        f.containerImage.version,
                        f.maximumConnections,
                        f.costs,
                        f.memoryLimit,
                        f.cpuShare)
            }
        }

        /**
         * Maps the configuration for a device type to an instance of the model.
         *
         * @param d device type emufog.config to map to an object
         * @return mapped device type object
         */
        private fun mapDeviceType(d: DeviceTypeConfig): DeviceType {
            return if (nullOrEmpty(d.containerImage.version)) {
                DeviceType(d.containerImage.name,
                           d.scalingFactor,
                           d.averageDeviceCount.toFloat(),
                           d.memoryLimit,
                           d.cpuShare)
            } else {
                DeviceType(d.containerImage.name,
                           d.containerImage.version,
                           d.scalingFactor,
                           d.averageDeviceCount.toFloat(),
                           d.memoryLimit,
                           d.cpuShare)
            }
        }
    }
}
