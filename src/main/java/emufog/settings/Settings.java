package emufog.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.util.ArrayList;
import java.util.List;

/**
 * The settings class contains all different settings used within the application.
 * An instance of this class can be read in from a valid settings file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private boolean overWriteOutputFile;

    private String applicationAssignmentPolicy;

    private String devicePlacement;

    private String edgeIdentifier;

    private String fogPlacement;

    private String reader;

    private String exporter;

    /****************************
     *  Basic Settings          *;
     ****************************/
    //base IPv4 address of the network's subnet
    private String baseAddress;

    //maximal number of fog nodes to place in the network
    private int maxFogNodes;

    // upper threshold of the cost function to limit the fog node placement
    private float costThreshold;

    // upper latency limit
    private float delayBoundary;

    // number of threads to use for the backbone and fog placement
    private int threadCount;

    // indicator whether the fog graph should be build in parallel
    private boolean parallelFogBuilding;

    private List<DeviceNodeType> deviceNodeTypes = new ArrayList<>();

    private List<FogNodeType> fogNodeTypes = new ArrayList<>();

    private List<Application> fogApplications = new ArrayList<>();

    private List<Application> deviceApplications = new ArrayList<>();


    public Settings() {
    }

    public static void read(Path settingsPath) throws FileNotFoundException {

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
     *
     * @return retruns settings object.
     * @throws Exception if Settings file is not yet instantiated.
     */
    public static Settings getSettings() {
        if (INSTANCE == null) try {
            throw new Exception("There is no Settings file instantiated yet!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return INSTANCE;
    }

    public String getApplicationAssignmentPolicy() {
        return applicationAssignmentPolicy;
    }

    public void setApplicationAssignmentPolicy(String applicationAssignmentPolicy) {
        this.applicationAssignmentPolicy = applicationAssignmentPolicy;
    }

    public String getDevicePlacement() {
        return devicePlacement;
    }

    public void setDevicePlacement(String devicePlacement) {
        this.devicePlacement = devicePlacement;
    }

    public String getEdgeIdentifier() {
        return edgeIdentifier;
    }

    public void setEdgeIdentifier(String edgeIdentifier) {
        this.edgeIdentifier = edgeIdentifier;
    }

    public String getFogPlacement() {
        return fogPlacement;
    }

    public void setFogPlacement(String fogPlacement) {
        this.fogPlacement = fogPlacement;
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
        return overWriteOutputFile;
    }

    public void setOverwriteExperimentFile(boolean overwriteExperimentFile) {
        this.overWriteOutputFile = overwriteExperimentFile;
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

    public boolean isOverWriteOutputFile() {
        return overWriteOutputFile;
    }

    public void setOverWriteOutputFile(boolean overWriteOutputFile) {
        this.overWriteOutputFile = overWriteOutputFile;
    }

    public List<DeviceNodeType> getDeviceNodeTypes() {
        return deviceNodeTypes;
    }

    public void setDeviceNodeTypes(List<DeviceNodeType> deviceNodeTypes) {
        this.deviceNodeTypes = deviceNodeTypes;
    }

    public List<FogNodeType> getFogNodeTypes() {
        return fogNodeTypes;
    }

    public void setFogNodeTypes(List<FogNodeType> fogNodeTypes) {
        this.fogNodeTypes = fogNodeTypes;
    }

    public List<Application> getFogApplications() {
        return fogApplications;
    }

    public void setFogApplications(List<Application> fogApplications) {
        this.fogApplications = fogApplications;
    }

    public List<Application> getDeviceApplications() {
        return deviceApplications;
    }

    public void setDeviceApplications(List<Application> deviceApplications) {
        this.deviceApplications = deviceApplications;
    }

    public String getExporter() {
        return exporter;
    }

    public void setExporter(String exporter) {
        this.exporter = exporter;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public float getDelayBoundary() {
        return delayBoundary;
    }

    public void setDelayBoundary(float delayBoundary) {
        this.delayBoundary = delayBoundary;
    }
}

