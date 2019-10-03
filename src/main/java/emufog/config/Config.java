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
package emufog.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import emufog.container.DeviceType;
import emufog.container.FogType;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static emufog.util.StringUtils.nullOrEmpty;

/**
 * Top level config object of the YAML document.
 */
public class Config {

    private static Config INSTANCE;

    public final String baseAddress;

    public final boolean overWriteOutputFile;

    public final int maxFogNodes;

    public final float costThreshold;

    public final float hostDeviceLatency;

    public final float hostDeviceBandwidth;

    public final List<DeviceType> deviceNodeTypes;

    public final List<FogType> fogNodeTypes;

    Config(
        @JsonProperty("base-address") String baseAddress,
        @JsonProperty("overwrite-experiment-file") boolean overWriteOutputFile,
        @JsonProperty("max-fog-nodes") int maxFogNodes,
        @JsonProperty("cost-threshold") float costThreshold,
        @JsonProperty("host-device-latency") float hostDeviceLatency,
        @JsonProperty("host-device-bandwidth") float hostDeviceBandwidth,
        @JsonProperty("device-node-types") Collection<DeviceTypeConfig> deviceNodeTypes,
        @JsonProperty("fog-node-types") Collection<FogTypeConfig> fogNodeTypes) {
        this.baseAddress = baseAddress;
        this.overWriteOutputFile = overWriteOutputFile;
        this.maxFogNodes = maxFogNodes;
        this.costThreshold = costThreshold;
        this.hostDeviceLatency = hostDeviceLatency;
        this.hostDeviceBandwidth = hostDeviceBandwidth;
        this.deviceNodeTypes = deviceNodeTypes.stream().map(Config::mapDeviceType).collect(Collectors.toList());
        this.fogNodeTypes = fogNodeTypes.stream().map(Config::mapFogType).collect(Collectors.toList());
    }

    public static void updateConfig(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("The given file path is not initialized.");
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.yaml");
        if (!matcher.matches(path)) {
            throw new IllegalArgumentException("The file ending does not match .yaml.");
        }

        // parse YAML document to a java object
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = mapper.readValue(path.toFile(), Config.class);
        if (config == null) {
            throw new IOException("Failed to parse the YAML file: " + path);
        }
        INSTANCE = config;
    }

    /**
     * Returns the currently active configuration object.
     *
     * @return current configuration
     */
    public static Config getConfig() {
        return INSTANCE;
    }

    /**
     * Maps the configuration for a fog type to an instance of the model.
     *
     * @param f fog type config to map to an object
     * @return mapped fog type object
     */
    private static FogType mapFogType(FogTypeConfig f) {
        if (nullOrEmpty(f.containerImage.version)) {
            return new FogType(f.containerImage.name, f.maximumConnections, f.costs, f.memoryLimit, f.cpuShare);
        } else {
            return new FogType(f.containerImage.name,
                f.containerImage.version,
                f.maximumConnections,
                f.costs,
                f.memoryLimit,
                f.cpuShare);
        }
    }

    /**
     * Maps the configuration for a device type to an instance of the model.
     *
     * @param d device type config to map to an object
     * @return mapped device type object
     */
    private static DeviceType mapDeviceType(DeviceTypeConfig d) {
        if (nullOrEmpty(d.containerImage.version)) {
            return new DeviceType(d.containerImage.name,
                d.scalingFactor,
                d.averageDeviceCount,
                d.memoryLimit,
                d.cpuShare);
        } else {
            return new DeviceType(d.containerImage.name,
                d.containerImage.version,
                d.scalingFactor,
                d.averageDeviceCount,
                d.memoryLimit,
                d.cpuShare);
        }
    }
}
