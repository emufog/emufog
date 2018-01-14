package emufog.settings;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import emufog.application.Application;
import emufog.nodeconfig.DeviceNodeType;
import emufog.nodeconfig.FogNodeType;
import emufog.util.Logger;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

// TODO: Complete settings class needs to be reworked.

// TODO: Future work implement Field for Dynamic class loading to select desired placement algorithm for each step.

/**
 * The settings class contains all different settings used within the application.
 * An instance of this class can be read in from a valid settings file.
 */
public class Settings {

    private static Settings INSTANCE;

    /****************************
     *  Input/Output Settings   *
     ****************************/
    // path to input topology
    private Path inputGraphFilePath;

    // path to export experiment to
    private Path exportFilePath;

    //indicates whether the output file can be overwritten or not
    private  boolean overwriteExperimentFile;

    /****************************
     *  Basic Settings          *
     ****************************/
    //base IPv4 address of the network's subnet
    private String baseAddress;

    //maximal number of fog nodes to place in the network
    private  int maxFogNodes;

    // upper threshold of the cost function to limit the fog node placement
    private  float costThreshold;

    // latency delay between a host device and the edge node
    private  float edgeDeviceDelay;

    // bandwidth between a host device and the edge node
    private  float edgeDeviceBandwidth;

    // number of threads to use for the backbone and fog placement
    private  int threadCount;

    // indicator whether the fog graph should be build in parallel
    private  boolean parallelFogBuilding;

    private  DeviceNodeType[] deviceNodeTypes;

    private  FogNodeType[] fogNodeTypes;

    private  Application[] fogApplications;

    private  Application[] deviceApplications;


    public Settings(){}

    public static void read(Path settingsPath) throws FileNotFoundException {

/*        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Settings.class, new SettingsDeserializer());
        gsonBuilder.registerTypeAdapter(FogNodeType.class, new FogNodeDeserializer());
        gsonBuilder.registerTypeAdapter(DeviceNodeType.class, new DeviceNodeDeserializer());
        gsonBuilder.registerTypeAdapter(Application.class, new ApplicationDeserializer());

        Gson gson = gsonBuilder.create();

        INSTANCE = gson.fromJson(new FileReader(settingsPath.toFile()), Settings.class);*/

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            INSTANCE = mapper.readValue(new File(settingsPath.toString()), Settings.class);
            Logger logger = Logger.getInstance();
            //Print Settings Object as String to check if parsing was successful.
            logger.log(ReflectionToStringBuilder.toString(INSTANCE, ToStringStyle.MULTI_LINE_STYLE));
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Returns singleton settings instance. If settings is null throws Exception.
     * @return
     * @throws Exception
     */
    public static Settings getInstance() throws Exception {
        if(INSTANCE == null) throw new Exception("There is no Settings file instantiated yet!");

        return INSTANCE;
    }

    public Path getInputGraphFilePath() {
        return inputGraphFilePath;
    }

    public void setInputGraphFilePath(Path inputGraphFilePath) {
        this.inputGraphFilePath = inputGraphFilePath;
    }

    public Path getExportFilePath() {
        return exportFilePath;
    }

    public void setExportFilePath(Path exportFilePath) {
        this.exportFilePath = exportFilePath;
    }

    public boolean isOverwriteExperimentFile() {
        return overwriteExperimentFile;
    }

    public void setOverwriteExperimentFile(boolean overwriteExperimentFile) {
        this.overwriteExperimentFile = overwriteExperimentFile;
    }

    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getMaxFogNodes() {
        return maxFogNodes;
    }

    public void setMaxFogNodes(int maxFogNodes) {
        this.maxFogNodes = maxFogNodes;
    }

    public float getCostThreshold() {
        return costThreshold;
    }

    public void setCostThreshold(float costThreshold) {
        this.costThreshold = costThreshold;
    }

    public float getEdgeDeviceDelay() {
        return edgeDeviceDelay;
    }

    public void setEdgeDeviceDelay(float edgeDeviceDelay) {
        this.edgeDeviceDelay = edgeDeviceDelay;
    }

    public float getEdgeDeviceBandwidth() {
        return edgeDeviceBandwidth;
    }

    public void setEdgeDeviceBandwidth(float edgeDeviceBandwidth) {
        this.edgeDeviceBandwidth = edgeDeviceBandwidth;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isParallelFogBuilding() {
        return parallelFogBuilding;
    }

    public void setParallelFogBuilding(boolean parallelFogBuilding) {
        this.parallelFogBuilding = parallelFogBuilding;
    }

    public DeviceNodeType[] getDeviceNodeTypes() {
        return deviceNodeTypes;
    }

    public void setDeviceNodeTypes(DeviceNodeType[] deviceNodeTypes) {
        this.deviceNodeTypes = deviceNodeTypes;
    }

    public FogNodeType[] getFogNodeTypes() {
        return fogNodeTypes;
    }

    public void setFogNodeTypes(FogNodeType[] fogNodeTypes) {
        this.fogNodeTypes = fogNodeTypes;
    }

    public Application[] getFogApplications() {
        return fogApplications;
    }

    public void setFogApplications(Application[] fogApplications) {
        this.fogApplications = fogApplications;
    }

    public Application[] getDeviceApplications() {
        return deviceApplications;
    }

    public void setDeviceApplications(Application[] deviceApplications) {
        this.deviceApplications = deviceApplications;
    }
}

