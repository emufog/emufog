package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.FogNode;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultApplicationAssignment implements IApplicationAssignmentPolicy {

    private List<Device> deviceList = new ArrayList<>();

    private List<FogNode> fogNodeList = new ArrayList<>();

    private List<Application> fogApplications = new ArrayList<>();

    private List<Application> deviceApplications = new ArrayList<>();

    @Override
    public void generateDeviceApplicationMapping(MutableNetwork topology) {

        //TODO: Work with subnets.
        try {
            deviceApplications = checkNotNull(Settings.getInstance().getDeviceApplications());
        } catch (Exception e) {
            e.printStackTrace(
            );
        }

        topology.nodes().stream().filter(n -> n instanceof Device).forEach(d -> deviceList.add(((Device) d)));

        //assign all deviceApplications to each device node
        for(Device device : deviceList){
            device.getConfiguration().setApplications(deviceApplications);
        }




    }

    @Override
    public void generateFogApplicationMapping(MutableNetwork topology) {

        topology.nodes().stream().filter(n -> n instanceof FogNode).forEach(f -> fogNodeList.add((FogNode) f));

        // get List of fogApplications
        try {
            fogApplications = checkNotNull(Settings.getInstance().getFogApplications());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //assign all fogNodeApplications to each fog node.
        for(FogNode fogNode : fogNodeList){
            fogNode.getConfiguration().setApplications(fogApplications);
        }

    }
}
