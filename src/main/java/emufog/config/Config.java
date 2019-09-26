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
package emufog.config;

import emufog.container.DeviceType;
import emufog.container.FogType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static emufog.util.StringUtils.nullOrEmpty;

/**
 * The config class contains all different config used within the application.
 * An instance of this class can be read in from a valid config file.
 */
public class Config {

    /* base IPv4 address of the network's subnet */
    public final String baseAddress;

    /* indicates whether the output file can be overwritten or not */
    public final boolean overwriteExperimentFile;

    /* list of all possible fog node types to deploy in the network */
    public final List<FogType> fogNodeTypes;

    /* list of all different device types to deploy at the edge of the network */
    public final List<DeviceType> deviceNodeTypes;

    /* maximal number of fog nodes to place in the network */
    public final int maxFogNodes;

    /* upper threshold of the cost function to limit the fog node placement */
    public final float costThreshold;

    /* latency delay between a host device and the edge node */
    public final float edgeDeviceDelay;

    /* bandwidth between a host device and the edge node */
    public final float edgeDeviceBandwidth;

    /* number of threads to use for the backbone and fog placement */
    public final int threadCount;

    /* indicator whether the fog graph should be build in parallel */
    public final boolean fogGraphParallel;

    public final boolean timeMeasuring;

    /**
     * Creates a new instance of the Config class using the read in config object.
     *
     * @param config config object containing the required information
     */
    Config(EmuFogConfig config) {
        baseAddress = config.baseAddress;
        overwriteExperimentFile = config.overWriteOutputFile;
        maxFogNodes = config.maxFogNodes;
        costThreshold = config.costThreshold;
        edgeDeviceDelay = config.hostDeviceLatency;
        edgeDeviceBandwidth = config.hostDeviceBandwidth;
        threadCount = config.threadCount;
        fogGraphParallel = config.paralleledFogBuilding;
        timeMeasuring = config.timeMeasuring;

        Map<Integer, FogType> fogTypes = new HashMap<>();
        for (FogTypeConfig type : config.fogNodeTypes) {
            fogTypes.put(type.id, mapFogType(type));
        }

        for (FogTypeConfig fogType : config.fogNodeTypes) {
            if (fogType.dependencies != null) {
                FogType fogNodeType = fogTypes.get(fogType.id);
                for (int id : fogType.dependencies) {
                    fogNodeType.addDependency(fogTypes.get(id));
                }
            }
        }
        fogNodeTypes = new ArrayList<>(fogTypes.values());

        deviceNodeTypes = config.deviceNodeTypes.stream().map(Config::mapDeviceType).collect(Collectors.toList());
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
            return new FogType(f.containerImage.name, f.containerImage.version, f.maximumConnections, f.costs, f.memoryLimit, f.cpuShare);
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
            return new DeviceType(d.containerImage.name, d.scalingFactor, d.averageDeviceCount, d.memoryLimit, d.cpuShare);
        } else {
            return new DeviceType(d.containerImage.name, d.containerImage.version, d.scalingFactor, d.averageDeviceCount, d.memoryLimit, d.cpuShare);
        }
    }
}
