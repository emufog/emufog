package emufog.topology;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import emufog.export.ITopologyExporter;
import emufog.export.MaxinetExporter;
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

    private void identifyEdge(){

        IEdgeIdentifier edgeIdentifier = new DefaultEdgeIdentifier();

        edgeIdentifier.identifyEdge(getTopology());

    }

    private void assignEdgeDevices() throws Exception {

        IDevicePlacement devicePlacement = new DefaultDevicePlacement();

        devicePlacement.assignEdgeDevices(getTopology(), settings.getDeviceNodeTypes());

    }

    private void createFogLayout() throws Exception {
        IFogLayout fogLayout = new DefaultFogLayout();
        fogLayout.identifyFogNodes(getTopology());
    }
    private void placeFogNodes(){

        IFogPlacement fogPlacement = new DefaultFogPlacement();
        fogPlacement.placeFogNodes(getTopology());

    }

    private void assignApplications(){

        IApplicationAssignmentPolicy applicationAssignmentPolicy = new DefaultApplicationAssignment();

        applicationAssignmentPolicy.generateDeviceApplicationMapping(getTopology());
        applicationAssignmentPolicy.generateFogApplicationMapping(getTopology());

    }

    public static class TopologyBuilder{

        public Topology build() throws IOException {
            return new Topology(this);
        }

    }

    public void export() throws IOException {

        final Path exportPath = settings.getExportFilePath();

        ITopologyExporter exporter = new MaxinetExporter();

        exporter.exportTopology(getTopology(), exportPath);

    }

}
