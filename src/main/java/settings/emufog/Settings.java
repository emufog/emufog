package settings.emufog;

import docker.emufog.DeviceType;
import docker.emufog.FogType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
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
     * Creates a new instance of the Settings class.
     *
     * @param baseAddress
     * @param overwriteExperimentFile
     * @param fogNodeTypes
     * @param deviceNodeTypes
     * @param maxFogNodes
     * @param costThreshold
     * @param edgeDeviceDelay
     * @param edgeDeviceBandwidth
     * @param threadCount
     * @param fogGraphParallel
     */
    private Settings(String baseAddress, boolean overwriteExperimentFile, List<FogType> fogNodeTypes,
                     List<DeviceType> deviceNodeTypes, int maxFogNodes, float costThreshold,
                     float edgeDeviceDelay, float edgeDeviceBandwidth, int threadCount, boolean fogGraphParallel) {
        this.baseAddress = baseAddress;
        this.overwriteExperimentFile = overwriteExperimentFile;
        this.fogNodeTypes = fogNodeTypes;
        this.deviceNodeTypes = deviceNodeTypes;
        this.maxFogNodes = maxFogNodes;
        this.costThreshold = costThreshold;
        this.edgeDeviceDelay = edgeDeviceDelay;
        this.edgeDeviceBandwidth = edgeDeviceBandwidth;
        this.threadCount = threadCount;
        this.fogGraphParallel = fogGraphParallel;
    }

    /**
     * Reads in the settings specified in an external XML file. The file has to valid according
     * to the XML schema file.
     *
     * @param path path to the settings file
     * @return the read in settings object
     * @throws IllegalArgumentException if the path object given is null
     * @throws XMLParsingException      in case there occurs an error while reading the XML file
     */
    public static Settings readSettings(Path path) throws IllegalArgumentException, XMLParsingException {
        if (path == null) {
            throw new IllegalArgumentException("The given file path is not initialized.");
        }

        Settings settings;

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(path.toFile());

            // normalize to ease the reading
            doc.normalize();

            Node addressNode = getFirstNode(doc, "BaseAddress");
            String baseAddress = addressNode.getTextContent();

            Node overwriteNode = getFirstNode(doc, "OverwriteOutputFile");
            boolean overwriteExperimentFile = Boolean.parseBoolean(overwriteNode.getTextContent());

            Node maxNode = getFirstNode(doc, "maxFogNodes");
            int maxNodes = Integer.parseInt(maxNode.getTextContent());

            Node costNode = getFirstNode(doc, "costThreshold");
            float costThreshold = Float.parseFloat(costNode.getTextContent());

            Node delayNode = getFirstNode(doc, "edgeDeviceDelay");
            float edgeDelay = Float.parseFloat(delayNode.getTextContent());

            Node bandwidthNode = getFirstNode(doc, "edgeDeviceBandwidth");
            float edgeBandwidth = Float.parseFloat(bandwidthNode.getTextContent());

            Node threadNode = getFirstNode(doc, "threadCount");
            int threadCount = Integer.parseInt(threadNode.getTextContent());

            Node fogParallelNode = getFirstNode(doc, "fogParallel");
            boolean fogParallel = Boolean.parseBoolean(fogParallelNode.getTextContent());

            List<FogType> fogTypes = new ArrayList<>();
            NodeList fogNodes = getFirstNode(doc, "fogNodeTypes").getChildNodes();
            for (int i = 0; i < fogNodes.getLength(); ++i) {
                Node fog = fogNodes.item(i);
                if (fog.getNodeType() == Node.ELEMENT_NODE) {
                    fogTypes.add(readFogType((Element) fog));
                }
            }

            List<DeviceType> deviceTypes = new ArrayList<>();
            NodeList deviceNodes = getFirstNode(doc, "deviceNodeTypes").getChildNodes();
            for (int i = 0; i < deviceNodes.getLength(); ++i) {
                Node device = deviceNodes.item(i);
                if (device.getNodeType() == Node.ELEMENT_NODE) {
                    deviceTypes.add(readDeviceType((Element) device));
                }
            }

            // create the actual settings object with the read in values
            settings = new Settings(baseAddress, overwriteExperimentFile, fogTypes, deviceTypes,
                    maxNodes, costThreshold, edgeDelay, edgeBandwidth, threadCount, fogParallel);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new XMLParsingException("Error while parsing the XML file.");
        }

        return settings;
    }

    /**
     * Reads in the information of an device docker type specified in the XML file.
     * Returns an instance of the DeviceType class.
     *
     * @param element XML element of the DOM tree
     * @return read in device docker type
     */
    private static DeviceType readDeviceType(Element element) {
        Node memoryNode = getFirstNode(element, "memoryLimit");
        int maxMemory = Integer.parseInt(memoryNode.getTextContent());

        Node cpuNode = getFirstNode(element, "cpuShare");
        float cpuShare = Float.parseFloat(cpuNode.getTextContent());

        Node dockerNode = getFirstNode(element, "dockerImage");
        assert dockerNode.getNodeType() == Node.ELEMENT_NODE : "docker XML node is invalid";
        String dockerImage = getDockerName((Element) dockerNode);

        Node scalingNode = getFirstNode(element, "scalingFactor");
        int scaling = Integer.parseInt(scalingNode.getTextContent());

        Node averageNode = getFirstNode(element, "averageDeviceCount");
        float averageDevices = Float.parseFloat(averageNode.getTextContent());

        return new DeviceType(dockerImage, scaling, averageDevices, maxMemory, cpuShare);
    }

    /**
     * Reads in the information of an fog docker type specified in the XML file.
     * Returns an instance of the FogType class.
     *
     * @param element XML element of the DOM tree
     * @return read in fog docker type
     */
    private static FogType readFogType(Element element) {
        Node clientsNode = getFirstNode(element, "maxClients");
        int maxClients = Integer.parseInt(clientsNode.getTextContent());

        Node costNode = getFirstNode(element, "costs");
        float costs = Float.parseFloat(costNode.getTextContent());

        Node memoryNode = getFirstNode(element, "memoryLimit");
        int maxMemory = Integer.parseInt(memoryNode.getTextContent());

        Node cpuNode = getFirstNode(element, "cpuShare");
        float cpuShare = Float.parseFloat(cpuNode.getTextContent());

        Node dockerNode = getFirstNode(element, "dockerImage");
        assert dockerNode.getNodeType() == Node.ELEMENT_NODE : "docker XML node is invalid";
        String dockerImage = getDockerName((Element) dockerNode);

        return new FogType(dockerImage, maxClients, costs, maxMemory, cpuShare);
    }

    /**
     * Reads in the Docker name of the image specified in the XML file.
     * The format is 'image:version'.
     *
     * @param element XML element of the DOM tree
     * @return name of the docker image
     */
    private static String getDockerName(Element element) {
        Node nameNode = getFirstNode(element, "name");
        Node versionNode = getFirstNode(element, "version");

        return nameNode.getTextContent() + ':' + versionNode.getTextContent();
    }

    /**
     * Returns the first node of the XML document that matches the given tag.
     *
     * @param doc DOM document to search
     * @param tag XML tag name to search for
     * @return the first node matching the pattern
     */
    private static Node getFirstNode(Document doc, String tag) {
        return doc.getElementsByTagName(tag).item(0);
    }

    /**
     * Returns the first node of the XML element that matches the given tag.
     *
     * @param element DOM element to search
     * @param tag     XML tag name to search for
     * @return the first node matching the pattern
     */
    private static Node getFirstNode(Element element, String tag) {
        return element.getElementsByTagName(tag).item(0);
    }
}
