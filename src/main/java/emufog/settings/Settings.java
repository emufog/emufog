package emufog.settings;

import emufog.docker.DeviceType;
import emufog.docker.FogType;

import java.util.ArrayList;
import java.util.List;

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

        fogNodeTypes = new ArrayList<>();
        for (SettingsReader.FogType fogType : json.FogNodeTypes) {
            fogNodeTypes.add(new FogType(fogType.DockerImage.toString(), fogType.MaximumConnections, fogType.Costs,
                    fogType.MemoryLimit, fogType.CPUShare));
        }
        deviceNodeTypes = new ArrayList<>();
        for (SettingsReader.DeviceType deviceType : json.DeviceNodeTypes) {
            deviceNodeTypes.add(new DeviceType(deviceType.DockerImage.toString(), deviceType.ScalingFactor,
                    deviceType.AverageDeviceCount, deviceType.MemoryLimit, deviceType.CPUShare));
        }
    }
}
