package emufog.topology;

import com.google.common.graph.MutableNetwork;
import emufog.export.ITopologyExporter;
import emufog.export.MaxinetExporter;
import emufog.placement.*;
import emufog.reader.*;
import emufog.settings.Settings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Topology {

    private MutableNetwork<Node,Link> topology;

    private Settings settings;

    private Map<Integer, AS> systems;


    private Topology(TopologyBuilder builder) throws IOException {

        this.settings = builder.settings;
        try {
            read();
            identifyEdge();
            placeFogNodes();
            assignApplications();
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private void read() throws IOException {

        TopologyReader reader = new BriteReader();

        this.topology = reader.parse(settings.getInputGraphFilePath());

    }

    private void identifyEdge(){

        IEdgeIdentifier edgeIdentifier = new DefaultEdgeIdentifier();

        edgeIdentifier.identifyEdge(topology);

    }

    private void placeFogNodes(){

        IFogLayout fogLayout = new DefaultFogLayout();

        fogLayout.identifyFogNodes(topology);
        fogLayout.placeFogNodes(topology);

    }

    private void assignApplications(){

        IApplicationAssignmentPolicy applicationAssignmentPolicy = new DefaultApplicationAssignment();

        applicationAssignmentPolicy.generateDeviceApplicationMapping(topology);
        applicationAssignmentPolicy.generateFogApplicationMapping(topology);

    }



    public static class TopologyBuilder{

        private Settings settings;

        public Topology build() throws IOException {
            return new Topology(this);
        }

        public TopologyBuilder setup(Settings settings){
            this.settings = settings;
            return this;
        }

    }

    public void export() throws IOException {

        final Path exportPath = settings.getExportFilePath();

        ITopologyExporter exporter = new MaxinetExporter();

        exporter.exportTopology(topology, exportPath);

    }

}
