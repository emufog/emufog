package emufog.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import emufog.application.Application;
import emufog.docker.DeviceType;
import emufog.docker.FogType;
import emufog.nodes.DeviceNode;
import emufog.nodes.FogNode;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
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
    private String baseAddress;

    /* indicates whether the output file can be overwritten or not */
    private  boolean overwriteExperimentFile;

    /* maximal number of fog nodes to place in the network */
    private  int maxFogNodes;

    /* upper threshold of the cost function to limit the fog node placement */
    private  float costThreshold;

    /* latency delay between a host device and the edge node */
    private  float edgeDeviceDelay;

    /* bandwidth between a host device and the edge node */
    private  float edgeDeviceBandwidth;

    /* number of threads to use for the backbone and fog placement */
    private  int threadCount;

    /* indicator whether the fog graph should be build in parallel */
    private  boolean fogGraphParallel;

    private  FogNode[] fogNodes;

    private  DeviceNode[] deviceNodes;

    private  Application[] applications;

    public Settings(){}

/*    *//**
     * Creates a new instance of the Settings class using the JSON object.
     * @param json
     * @param imageFile
     *//*
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
            fogTypes.put(fogType.ID, new FogType(*//*fogType.DockerImage.toString(),*//* fogType.MaximumConnections, fogType.Costs,
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
            deviceNodeTypes.add(new DeviceType(*//*deviceType.DockerImage.toString(),*//* deviceType.ScalingFactor,
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
    }*/

    public Settings read(Path settingsPath) throws FileNotFoundException {

        Settings settings = null;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Settings.class, new SettingsDeserializer());
        gsonBuilder.registerTypeAdapter(FogNode.class, new FogNodeDeserializer());
        gsonBuilder.registerTypeAdapter(DeviceNode.class, new DeviceNodeDeserializer());
        gsonBuilder.registerTypeAdapter(Application.class, new ApplicationDeserializer());

        Gson gson = gsonBuilder.create();

        settings = gson.fromJson(new FileReader(settingsPath.toFile()), Settings.class);

        return settings;
    }


    protected void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }

    protected void setOverwriteExperimentFile(boolean overwriteExperimentFile) {
        this.overwriteExperimentFile = overwriteExperimentFile;
    }

    protected void setMaxFogNodes(int maxFogNodes) {
        this.maxFogNodes = maxFogNodes;
    }

    protected void setCostThreshold(float costThreshold) {
        this.costThreshold = costThreshold;
    }

    protected void setEdgeDeviceDelay(float edgeDeviceDelay) {
        this.edgeDeviceDelay = edgeDeviceDelay;
    }

    protected void setEdgeDeviceBandwidth(float edgeDeviceBandwidth) {
        this.edgeDeviceBandwidth = edgeDeviceBandwidth;
    }

    protected void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    protected void setFogGraphParallel(boolean fogGraphParallel) {
        this.fogGraphParallel = fogGraphParallel;
    }

    protected void setFogNodes(FogNode[] fogNodes) {
        this.fogNodes = fogNodes;
    }

    protected void setDeviceNodes(DeviceNode[] deviceNodes) {
        this.deviceNodes = deviceNodes;
    }

    protected void setApplications(Application[] applications) {
        this.applications = applications;
    }

    public String getBaseAddress() {
        return baseAddress;
    }

    public boolean isOverwriteExperimentFile() {
        return overwriteExperimentFile;
    }

    public int getMaxFogNodes() {
        return maxFogNodes;
    }

    public float getCostThreshold() {
        return costThreshold;
    }

    public float getEdgeDeviceDelay() {
        return edgeDeviceDelay;
    }

    public float getEdgeDeviceBandwidth() {
        return edgeDeviceBandwidth;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public boolean isFogGraphParallel() {
        return fogGraphParallel;
    }

    public FogNode[] getFogNodes() {
        return fogNodes;
    }

    public DeviceNode[] getDeviceNodes() {
        return deviceNodes;
    }

    public Application[] getApplications() {
        return applications;
    }
}
