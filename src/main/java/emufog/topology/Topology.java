package emufog.topology;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import emufog.export.ContainernetExporter;
import emufog.export.ITopologyExporter;
import emufog.placement.*;
import emufog.reader.BriteReader;
import emufog.reader.TopologyReader;
import emufog.settings.Settings;
import emufog.util.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class Topology {

    private static MutableNetwork<Node, Link> INSTANCE;

    private Settings settings;

    public static MutableNetwork<Node, Link> getTopology(){
        if (INSTANCE == null) {
            INSTANCE = NetworkBuilder.undirected().allowsParallelEdges(false).build();
        }
        return INSTANCE;
    }

    private Topology(TopologyBuilder builder) throws IOException {

        Logger logger = Logger.getInstance();

        try {
            settings = Settings.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            long start = System.nanoTime();
            read();
            long end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + "ms to read the topology");
            logger.log("Number of nodes: " + getTopology().nodes().size());
            logger.log("Number of edges: " + getTopology().edges().size());

            start = System.nanoTime();
            identifyEdge();
            end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + "ms to identify the Edge");

            start = System.nanoTime();
            assignEdgeDevices();
            end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + "ms to place the Devices");

            start = System.nanoTime();
            createFogLayout();
            end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + "ms to create the FogLayout");

           /* start = System.nanoTime();
            placeFogNodes();
            end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + "ms to place the FogNodes");*/

            start = System.nanoTime();
            assignApplications();
            end = System.nanoTime();
            logger.log("It took " + Logger.convertToMs(start,end) + "ms to assignApplications to devices and fog nodes");

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void read() throws IOException {

        TopologyReader reader = new BriteReader();

        INSTANCE = reader.parse(settings.getInputGraphFilePath());

    }

    /**
     * Edge identification policy. Desired implementation is loaded dynamically from settings file.
     * Fallback is DefaultEdgeIdentifier.
     * @throws Exception
     */
    private void identifyEdge() throws Exception {

        // fall back to default policy if no preference is set in the settings.
        if(settings.getEdgeIdentifier() == null){

            IEdgeIdentifier edgeIdentifier = new DefaultEdgeIdentifier();

            edgeIdentifier.identifyEdge(getTopology());


        } else {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class edgeIdentifierClass = classLoader.loadClass(settings.getEdgeIdentifier());

            Object edgeIdentiferObject = edgeIdentifierClass.newInstance();

            ((IEdgeIdentifier) edgeIdentiferObject).identifyEdge(getTopology());
        }


    }

    /**
     * Device placement policy. Desired implementation is loaded dynamically from settings file.
     * Fallback is DefaultDevicePlacement.
     * @throws Exception
     */
    private void assignEdgeDevices() throws Exception {

        if(settings.getEdgeIdentifier() == null){

            IDevicePlacement devicePlacement = new DefaultDevicePlacement();

            devicePlacement.assignEdgeDevices(getTopology(), settings.getDeviceNodeTypes());

        } else {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class devicePlacementClass = classLoader.loadClass(settings.getDevicePlacement());

            Object devicePlacementObject = devicePlacementClass.newInstance();

            ((IDevicePlacement) devicePlacementObject).assignEdgeDevices(getTopology(), settings.getDeviceNodeTypes());
        }
    }

    /**
     * Fog node placement policy. Desired implementation is dynamically loaded from settings file.
     * Fallback is DefaultFogLayout.
     * @throws Exception
     */
    private void createFogLayout() throws Exception {

        if(settings.getFogPlacement() == null){

            IFogLayout fogLayout = new DefaultFogLayout();

            fogLayout.identifyFogNodes(getTopology());

        } else {

            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class fogLayoutClass = classLoader.loadClass(settings.getFogPlacement());

            Object fogPlacementObject = fogLayoutClass.newInstance();

            ((IFogLayout) fogPlacementObject).identifyFogNodes(getTopology());
        }



    }

    //TODO: Remove this unused method.
    private void placeFogNodes(){

        IFogPlacement fogPlacement = new DefaultFogPlacement();
        fogPlacement.placeFogNodes(getTopology());

    }

    /**
     * Application assignment policy. Desired implementation is loaded from settings file.
     * Fallback implementation is DefaultApplicationAssignment.
     * @throws Exception
     */
    private void assignApplications() throws Exception {

        if(settings.getApplicationAssignmentPolicy() == null){

            IApplicationAssignmentPolicy applicationAssignmentPolicy = new DefaultApplicationAssignment();

            applicationAssignmentPolicy.generateDeviceApplicationMapping(getTopology());
            applicationAssignmentPolicy.generateFogApplicationMapping(getTopology());


        } else {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class assignApplicationsClass = classLoader.loadClass(settings.getApplicationAssignmentPolicy());

            Object assignApplicationsObject = assignApplicationsClass.newInstance();

            ((IApplicationAssignmentPolicy) assignApplicationsObject).generateFogApplicationMapping(getTopology());
            ((IApplicationAssignmentPolicy) assignApplicationsObject).generateDeviceApplicationMapping(getTopology());
        }


    }

    public static class TopologyBuilder{

        public Topology build() throws IOException {
            return new Topology(this);
        }

    }

    /**
     * Topology exporter. Desired exporter implementation is dynamically loaded from settings file.
     * Default fallback is containernet exporter.
     * @throws Exception
     */
    public void export() throws Exception {

        final Path exportPath = settings.getExportFilePath();

        // fall back to default exporter
        if(settings.getExporter() == null){

            ITopologyExporter exporter = new ContainernetExporter();

            exporter.exportTopology(getTopology(), exportPath);

        } else {

            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class exporterClass = classLoader.loadClass(settings.getExporter());

            Object exporter = exporterClass.newInstance();

            ((ITopologyExporter) exporter).exportTopology(getTopology(), exportPath);
        }
    }

}
