package emufog.export;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.container.Docker;
import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.FogNodeConfiguration;
import emufog.settings.Settings;
import emufog.topology.*;
import emufog.util.UniqueIDProvider;
import emufog.util.UniqueIPProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ContainernetExporter implements ITopologyExporter{

    private final List<String> lines;

    private final String blankLine;

    private List<Router> routerList;

    private List<Device> deviceList;

    private List<FogNode> fogNodeList;

    public ContainernetExporter(){
        lines = new ArrayList<>();
        routerList = new ArrayList<>();
        deviceList = new ArrayList<>();
        fogNodeList = new ArrayList<>();
        blankLine = "";
    }

    @Override
    public void exportTopology(MutableNetwork<Node, Link> topology, Path path) throws IOException {

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
            setupContainernetImports("info");

            addBlankLine();

            setupContainernetExperiment();

            addBlankLine();

            addRouters();

            addDevices(topology);

            addFogNodes(topology);

            addLinksBetweenRouters(topology);

            startContainernetExperiment();

            // set the overwrite option if feature is set in the settings file
            StandardOpenOption overwrite = Settings.getInstance().isOverwriteExperimentFile() ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;

            // write output in UTF-8 to the specified file
            Files.write(experimentFile.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, overwrite);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO: Generalize. Method is used in more than one class.
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

    /**
     *
     * @param logLevel
     */
    private void setupContainernetImports(String logLevel){
        lines.add("#!/usr/bin/python");
        lines.add("from mininet.net import Containernet");
        lines.add("from mininet.node import Controller");
        lines.add("from mininet.cli import CLI");
        lines.add("from mininet.link import TCLink");
        lines.add("from mininet.log import info, setLogLevel");
        lines.add("setLogLevel('"+ logLevel +"')");

    }

    private void setupContainernetExperiment(){
        addBlankLine();
        lines.add("net = Containernet(controller=Controller)");
        lines.add("info('*** Adding controller'\n)");
        lines.add("net.addController('c0')");
    }

    private void addRouters(){
        addBlankLine();
        lines.add("info('*** Adding switches'\n)");

        for(Router router : checkNotNull(routerList)){
            lines.add("# " + router.getType().toString());
            lines.add(router.getName() + " = net.addSwitch('"+ router.getName() +"')");
        }
    }

    private void addDevices(MutableNetwork<Node,Link> t){
        addBlankLine();
        lines.add("info('*** Adding devices'\n)");

        for(Device device : deviceList){

            Router accessPoint = null;

            //TODO: Check iteration. What happens if device is connected to multiple routers?
            for(Node node : t.adjacentNodes(device)){
                if(node instanceof Router){
                    accessPoint = (Router) node;
                }
            }

            lines.add("# " + device.getName());
            addCommentSeparatorLine();
            createMultiTierDeviceNode(device, checkNotNull(accessPoint));

        }
    }

    private void addFogNodes(MutableNetwork<Node, Link> t){
        addBlankLine();
        lines.add("info('*** Adding Fog Nodes'\n)");

        for(FogNode fogNode : fogNodeList){

            Router accessPoint = null;

            for(Node node : t.adjacentNodes(fogNode)){
                if(node instanceof Router){
                    accessPoint = (Router) node;
                }
            }

            addBlankLine();
            lines.add("# " + fogNode.getName());
            addCommentSeparatorLine();
            createMultiTierFogNode(fogNode, checkNotNull(accessPoint));
        }
    }

    private void createMultiTierDeviceNode(Device device, Router accessPoint){

        createMultiTierSwitch(device, accessPoint);

        DeviceNodeConfiguration configuration = device.getConfiguration();

        List<Application> applications = configuration.getApplications();

        for(Application application : applications){

            Docker container = checkNotNull(application.getContainer());

            //TODO: Assign unique ids to each application.
            //get unique id for application node
            int applicationId = UniqueIDProvider.getInstance().getNextID();
            UniqueIDProvider.getInstance().markIDused(applicationId);

            String name = device.getName() + applicationId;
            addBlankLine();

            lines.add("# " + application.getName());
            addDockerHost(name, UniqueIPProvider.getInstance().getNextIPV4Address(), container.getImage(), device.getDeviceNodeType().getMemoryLimit());

            connectApplicationToSwitch(device, name);
        }

    }

    private void createMultiTierFogNode(FogNode fogNode, Router accessPoint){

        createMultiTierSwitch(fogNode, accessPoint);

        FogNodeConfiguration configuration = fogNode.getConfiguration();

        List<Application> applications = configuration.getApplications();

        for(Application application : applications){

            Docker container = checkNotNull(application.getContainer());

            //TODO: Assign unique ids to each application.
            //get unique id for application node
            int applicationId = UniqueIDProvider.getInstance().getNextID();
            UniqueIDProvider.getInstance().markIDused(applicationId);

            String name = fogNode.getName() + applicationId;
            addBlankLine();


            lines.add("# " + application.getName());
            addDockerHost(name, UniqueIPProvider.getInstance().getNextIPV4Address(), container.getImage(), fogNode.getFogNodeType().getMemoryLimit());

            connectApplicationToSwitch(fogNode, name);
        }

    }

    private void createMultiTierSwitch(Node node, Router accessPoint){
        addBlankLine();
        lines.add("# createMultitierSwitch for " + node.getName());
        lines.add("info('*** Create multi tier switch for "+ node.getName() +" '\n)");
        lines.add("mts" + node.getName() + " = net.addSwitch(\"" + "mts" + node.getName() + "\")");
        //connect to original topology router
        addLink("mts" + node.getName(), accessPoint.getName(), 0, 1000);
    }

    private void connectApplicationToSwitch(Node node, String name){
        addBlankLine();
        lines.add("# connect application to " + " r" + node.getName());
        lines.add("info('*** connect application to "  + " r" + node.getName()+"'\n)");
        addLink(name, "r" + node.getName(), 0, 1000);
    }

    /**
     * Create a new docker host in experiment.
     * @param nodeName
     * @param ip
     * @param dockerImage
     * @param memoryLimit
     */
    private void addDockerHost(String nodeName, String ip, String dockerImage, int memoryLimit){
        lines.add("info('*** Adding docker container "+ nodeName + " with " + dockerImage +"'\n)");
        lines.add(nodeName + " = net.addDocker(" + "'" + nodeName + "', ip='" + ip +"', dimage=\"" + dockerImage + "\", mem_limit=" + memoryLimit + ")");
    }

    private void addLinksBetweenRouters(MutableNetwork<Node, Link> t){
        addBlankLine();
        lines.add("# add links between routers");
        addCommentSeparatorLine();
        lines.add("info('*** Creating links between routers'\n)");

        for(Link link : t.edges()){

            EndpointPair<Node> endpointPair = t.incidentNodes(link);

            //only add subset of links that connects routers to each other. Device and FogNode connections are set in their respective Methods.
            if(endpointPair.nodeU() instanceof  Router && endpointPair.nodeV() instanceof Router){
                addLink(endpointPair.nodeU().getName(), endpointPair.nodeV().getName(), link.getDelay(), link.getBandwidth());
            }

        }

    }

    private void startContainernetExperiment(){
        addBlankLine();
        lines.add("info('*** Starting network'\n)");
        lines.add("net.start()");
        lines.add("info('*** Running CLI'\n)");
        lines.add("CLI(net)");
        lines.add("info('*** Stopping network'\n)");
        lines.add("net.stop()");
    }



    private void addLink(String source, String destination, float latency, float bandwidth){
        lines.add("info('*** Adding link from " + source + " to " + destination+ "'\n)");
        lines.add("net.addLink(" + source + ", " + destination + ", cls=TCLink, delay='" + latency + "ms', bw=" + bandwidth + ")");
    }

    private void addBlankLine(){
        lines.add(blankLine);
    }

    private void addCommentSeparatorLine(){
        lines.add("################################");
    }
}
