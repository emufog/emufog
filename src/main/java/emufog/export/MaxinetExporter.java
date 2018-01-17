package emufog.export;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.container.Docker;
import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.FogNodeConfiguration;
import emufog.settings.Settings;
import emufog.topology.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class MaxinetExporter implements ITopologyExporter{

    private final List<String> lines;

    private final String blankLine;

    private List<Router> routerList;

    private List<Device> deviceList;

    private List<FogNode> fogNodeList;

    public MaxinetExporter() {
       lines = new ArrayList<>();
       routerList = new ArrayList<>();
       deviceList = new ArrayList<>();
       fogNodeList = new ArrayList<>();
       blankLine = "";
    }

    @Override
    public void exportTopology(MutableNetwork<Node, Link> topology, Path path) throws IOException {

        //TODO: Implement exporter with new FogNode representation logic.

        filterTopology(checkNotNull(topology));

        File experimentFile = checkNotNull(path).toFile();

        try {
            //get configuration for overwrite permission
            boolean isOverwirteAllowed = Settings.getInstance().isOverwriteExperimentFile();
            if(!isOverwirteAllowed && experimentFile.exists()){
                throw new IllegalArgumentException("The given file already exist. Please provide a valid path");
            }

            // check the file ending of the given path
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.py");
            if (!matcher.matches(path)) {
                throw new IllegalArgumentException("The file name for MaxiNet has to be a python file (.py)");
            }

            // initialize empty sets to start the writing
            lines.clear();

            // begin to write the python file
            setupImports();

            addBlankLine();

            lines.add("topo = Topo()");

            addRouters();

            addDevices(topology);

            addFogNodes(topology);

            addLinks(topology);

            setupExperiment();

            // set the overwrite option if feature is set in the settings file
            StandardOpenOption overwrite = Settings.getInstance().isOverwriteExperimentFile() ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;

            // write output in UTF-8 to the specified file
            Files.write(experimentFile.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, overwrite);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterate over topology and sort nodes into corresponding list.
     * @param t topology to work on.
     */
    private void filterTopology(MutableNetwork<Node,Link> t){
        for(Node node : t.nodes()) {
            if (node instanceof emufog.topology.Router) {
                routerList.add((Router) node);
            }
            if (node instanceof Device) {
                deviceList.add((Device) node);
            }
            if (node instanceof FogNode) {
                fogNodeList.add((FogNode) node);
            }
        }
    }

    private void addRouters(){
        addBlankLine();
        lines.add("# add routers:");

        for(Router router : checkNotNull(routerList)){
            lines.add("# " + router.getType().toString());
            lines.add(router.getName() + " = topo.addSwitch(\"" + router.getName() + "\")");
        }
    }

    private void addDevices(MutableNetwork<Node,Link> t){
        addBlankLine();
        lines.add("# add devices");

        for(Device device : deviceList){

            Router accessPoint = null;

            for(Node node : t.adjacentNodes(device)){
                if(node instanceof Router){
                    accessPoint = (Router) node;
                }
            }

            createMultiTierDeviceNode(device, checkNotNull(accessPoint));

        }

    }

    private void addFogNodes(MutableNetwork<Node,Link> t){
        addBlankLine();
        lines.add("# add fogNodes");

        for(FogNode fogNode : fogNodeList){

            Router accessPoint = null;

            for(Node node : t.adjacentNodes(fogNode)){
                if(node instanceof Router){
                    accessPoint = (Router) node;
                }
            }

            createMultiTierFogNode(fogNode, accessPoint);
        }

    }

    private void createMultiTierDeviceNode(Device device, Router accessPoint){


        createMultiTierSwitch(device, accessPoint);

        DeviceNodeConfiguration configuration = device.getConfiguration();

        List<Application> applications = configuration.getApplications();

        for(Application application : applications){

            Docker container = checkNotNull(application.getContainer());

            String name = device.getName() + "_" + application.getName();

            addBlankLine();
            lines.add("# " + application.getName());
            addDockerHost(name, application.getIP(), container.getImage(), device.getDeviceNodeType().getMemoryLimit());

            connectApplicationToSwitch(device, name);

        }

    }

    private void createMultiTierSwitch(Node node, Router accessPoint){
        addBlankLine();
        lines.add("# createMultiTierSwitch for " + node.getName());
        lines.add( "r" + node.getName() + " = topo.addSwitch(\"" + "r" + node.getName() + "\")");
        //connect to original topology router.
        addLink( "r" + node.getName(), accessPoint.getName(), 0, 1000);
    }

    private void connectApplicationToSwitch(Node node, String name){
        addBlankLine();
        lines.add("# connect application to " + "r" + node.getName());
        addLink(name, "r" + node.getName(), 0, 1000);
    }

    private void createMultiTierFogNode(FogNode fogNode, Router accessPoint){

        createMultiTierSwitch(fogNode, accessPoint);

        FogNodeConfiguration configuration = fogNode.getConfiguration();

        List<Application> applications = configuration.getApplications();

        for(Application application : applications){

            Docker container = checkNotNull(application.getContainer());

            String name = fogNode.getName() + "_" + application.getName();

            addBlankLine();
            lines.add("# " + application.getName());

            addDockerHost(name, application.getIP(), container.getImage(), fogNode.getFogNodeType().getMemoryLimit());

        }

    }

    //TODO: Generalize addDockerHost method. Pass in complete Node object instead of specific fields.
    /**
     * Create a new docker host in experiment.
     * @param nodeName
     * @param ip
     * @param dockerImage
     * @param memoryLimit
     */
    private void addDockerHost(String nodeName, String ip, String dockerImage, int memoryLimit){

        lines.add(nodeName + " = topo.addHost(\"" +nodeName + "\", cls=Docker, ip=\"" + ip +
                "\", dimage=\"" + dockerImage + "\", mem_limit=" + memoryLimit + ")");
    }

    /**
     * Iterates over the edges of the given topology and retrieves the endpoint pair for
     * each link and creates new link in experiment file.
     * @param t topology to work on.
     */
    private void addLinks(MutableNetwork<Node, Link> t){
        addBlankLine();
        lines.add("# add links");

        for(Link link : t.edges()){

            EndpointPair<Node> endpointPair = t.incidentNodes(link);

            addLink(endpointPair.nodeU().getName(), endpointPair.nodeV().getName(), link.getDelay(), link.getBandwidth());
        }
    }

    /**
     * Adds a new link between two nodes to the document.
     *
     * @param source      source of the link
     * @param destination destination of the link
     * @param latency     latency applied to this link
     * @param bandwidth   bandwidth limitations of this link
     */
    private void addLink(String source, String destination, float latency, float bandwidth) {
        lines.add("topo.addLink(" + source + ", " + destination +
                ", delay='" + latency + "ms', bw=" + bandwidth + ")");
    }

    /**
     *  Writes the necessary imports at the top of the output file.
     */
    private void setupImports(){
        lines.add("#!/usr/bin/env python2");
        addBlankLine();
        lines.add("import time");
        addBlankLine();
        lines.add("from MaxiNet.Frontend import maxinet");
        lines.add("from MaxiNet.Frontend.container import Docker");
        lines.add("from mininet.topo import Topo");
        lines.add("from mininet.node import OVSSwitch");
    }

    /**
     * Writes the lines to setup and start an experiment in MaxiNet.
     */
    private void setupExperiment(){
        addBlankLine();
        lines.add("# create experiment");
        lines.add("cluster = maxinet.Cluster()");
        lines.add("exp = maxinet.Experiment(cluster, topo, switch=OVSSwitch)");
        lines.add("exp.setup()");
    }

    private void addBlankLine(){
        lines.add(blankLine);
    }
}
