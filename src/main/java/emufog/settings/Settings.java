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
package emufog.settings;

import emufog.docker.DeviceType;
import emufog.docker.FogType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The settings class contains all different settings used within the application.
 * An instance of this class can be read in from a valid settings file.
 */
public class Settings {

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

    /**
     * Creates a new instance of the Settings class using the JSON object.
     *
     * @param json JSON object containing the required information
     */
    Settings(SettingsReader.JSONSettings json) {
        baseAddress = json.BaseAddress;
        overwriteExperimentFile = json.OverWriteOutputFile;
        maxFogNodes = json.MaxFogNodes;
        costThreshold = json.CostThreshold;
        edgeDeviceDelay = json.HostDeviceLatency;
        edgeDeviceBandwidth = json.HostDeviceBandwidth;
        threadCount = json.ThreadCount;
        fogGraphParallel = json.ParalleledFogBuilding;

        Map<Integer, FogType> fogTypes = new HashMap<>();
        for (SettingsReader.FogType fogType : json.FogNodeTypes) {
            fogTypes.put(fogType.ID, new FogType(fogType.DockerImage.toString(), fogType.MaximumConnections, fogType.Costs,
                    fogType.MemoryLimit, fogType.CPUShare));
        }
        for (SettingsReader.FogType fogType : json.FogNodeTypes) {
            FogType fogNodeType = fogTypes.get(fogType.ID);
            if (fogType.Dependencies != null) {
                for (int id : fogType.Dependencies) {
                    fogNodeType.addDependency(fogTypes.get(id));
                }
            }
        }
        fogNodeTypes = new ArrayList<>(fogTypes.values());

        deviceNodeTypes = new ArrayList<>();
        for (SettingsReader.DeviceType deviceType : json.DeviceNodeTypes) {
            deviceNodeTypes.add(new DeviceType(deviceType.DockerImage.toString(), deviceType.ScalingFactor,
                    deviceType.AverageDeviceCount, deviceType.MemoryLimit, deviceType.CPUShare));
        }
    }
}
