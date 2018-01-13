package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.FogNode;

import java.util.List;

import static emufog.topology.Types.ApplicationType.DEVICE_APPLICATION;
import static emufog.topology.Types.ApplicationType.FOG_APPLICATION;

public class DefaultApplicationAssignment implements IApplicationAssignmentPolicy {

    private List<Device> deviceList = null;

    private List<FogNode> fogNodeList = null;

    private List<Application> applications = null;

    private List<Application> fogApplications = null;

    private List<Application> deviceApplications = null;

    @Override
    public void generateDeviceApplicationMapping(MutableNetwork topology) {

        try {
            applications = Settings.getInstance().getApplications();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // split applications into fogApplications and deviceApplications
        //TODO: Parse specified applications into two separate lists. One for each type.
        for(Application application : applications){
            if(application.getApplicationType().equals(DEVICE_APPLICATION)){
                deviceApplications.add(application);
            }
            if(application.getApplicationType().equals(FOG_APPLICATION)){
                fogApplications.add(application);
            }
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
