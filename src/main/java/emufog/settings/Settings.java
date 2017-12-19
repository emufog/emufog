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
    

	public final List<String> fogImages;

	public final List<String> deviceImages;

    /**
     * Creates a new instance of the Settings class using the JSON object.
     *
     * @param json JSON object containing the required information
     */
    Settings(SettingsReader.JSONSettings json, SettingsReader.JSONImages imageFile) {
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
            fogTypes.put(fogType.ID, new FogType(/*fogType.DockerImage.toString(),*/ fogType.MaximumConnections, fogType.Costs,
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
            deviceNodeTypes.add(new DeviceType(/*deviceType.DockerImage.toString(),*/ deviceType.ScalingFactor,
                    deviceType.AverageDeviceCount, deviceType.MemoryLimit, deviceType.CPUShare));
        }
        fogImages = new ArrayList<>();
        deviceImages = new ArrayList<>();
        for (SettingsReader.DockerName name: imageFile.FogImages) {
        	fogImages.add(name.toString());
        }
        for (SettingsReader.DockerName name: imageFile.DeviceImages) {
        	deviceImages.add(name.toString());
        }
    }
}

/*
TODO

Why differentiate between fog and device Images?


 */