package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.FogNode;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultApplicationAssignment implements IApplicationAssignmentPolicy {

    private List<Device> deviceList = null;

    private List<FogNode> fogNodeList = null;

    private List<Application> fogApplications = null;

    private List<Application> deviceApplications = null;

    @Override
    public void generateDeviceApplicationMapping(MutableNetwork topology) {


        try {
            deviceApplications = checkNotNull(Settings.getInstance().getDeviceApplications());
        } catch (Exception e) {
            e.printStackTrace();
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

        //assign all fogNodeApplications to each fog node.
        for(FogNode fogNode : fogNodeList){
            fogNode.getConfiguration().setApplications(fogApplications);
        }

    }
}
