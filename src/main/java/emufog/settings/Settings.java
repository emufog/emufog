package emufog.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import emufog.application.Application;
import emufog.nodeconfig.DeviceNodeType;
import emufog.nodeconfig.FogNodeType;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * The settings class contains all different settings used within the application.
 * An instance of this class can be read in from a valid settings file.
 */
public class Settings {

    private static Settings INSTANCE;

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

    private  FogNodeType[] fogNodes;

    private  DeviceNodeType[] deviceNodes;


    private  Application[] applications;

    private Path inputGraphFilePath;

    public Path getExportFilePath() {
        return exportFilePath;
    }

    private Path exportFilePath;

    public Settings(){}

    public static void read(Path settingsPath) throws FileNotFoundException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Settings.class, new SettingsDeserializer());
        gsonBuilder.registerTypeAdapter(FogNodeType.class, new FogNodeDeserializer());
        gsonBuilder.registerTypeAdapter(DeviceNodeType.class, new DeviceNodeDeserializer());
        gsonBuilder.registerTypeAdapter(Application.class, new ApplicationDeserializer());

        Gson gson = gsonBuilder.create();

        INSTANCE = gson.fromJson(new FileReader(settingsPath.toFile()), Settings.class);
    }

    public static Settings getInstance() throws Exception {
        if(INSTANCE == null) throw new Exception("There is no Settings file instantiated yet!");

        return INSTANCE;
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

    protected void setFogNodes(FogNodeType[] fogNodes) {
        this.fogNodes = fogNodes;
    }

    protected void setDeviceNodes(DeviceNodeType[] deviceNodes) {
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

    public List<FogNodeType> getFogNodes() {
        return Arrays.asList(fogNodes);
    }

    public List<DeviceNodeType> getDeviceNodes() {
        return Arrays.asList(deviceNodes);
    }

    public List<Application> getApplications() {
        return Arrays.asList(applications);
    }

    public Path getInputGraphFilePath() {
        return inputGraphFilePath;
    }
}

