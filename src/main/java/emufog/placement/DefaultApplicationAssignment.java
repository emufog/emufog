package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.FogNode;
import emufog.util.Logger;
import emufog.util.UniqueIDProvider;
import emufog.util.UniqueIPProvider;

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

        try {
            deviceApplications = checkNotNull(Settings.getSettings().getDeviceApplications());
        } catch (Exception e) {
            e.printStackTrace(
            );
        }

        assignIpAndIds(deviceApplications);

        topology.nodes().stream().filter(n -> n instanceof Device).forEach(d -> deviceList.add(((Device) d)));

        //assign all deviceApplications to each device node
        for(Device device : deviceList){
            device.getConfiguration().setApplications(deviceApplications);
        }

        Logger logger = Logger.getInstance();
        int count = 0;
        for(Device device : deviceList){
            count += device.getConfiguration().getApplications().size();
        }
        logger.log(String.format("Assigned %d applications to devices\n", count));


    }

    @Override
    public void generateFogApplicationMapping(MutableNetwork topology) {

        topology.nodes().stream().filter(n -> n instanceof FogNode).forEach(f -> fogNodeList.add((FogNode) f));

        // get List of fogApplications
        try {
            fogApplications = checkNotNull(Settings.getSettings().getFogApplications());
        } catch (Exception e) {
            e.printStackTrace();
        }

        assignIpAndIds(fogApplications);

        //assign all fogNodeApplications to each fog node.
        for(FogNode fogNode : fogNodeList){
            fogNode.getConfiguration().setApplications(fogApplications);
        }

        Logger logger = Logger.getInstance();
        int count = 0;
        for(FogNode fogNode : fogNodeList){
            count += fogNode.getConfiguration().getApplications().size();
        }
        logger.log(String.format("Assigned %d applications to fog nodes\n", count));

    }

    /**
     * Assignees a unique id to each application and adds unique ip address if not already defined in the settings file.
     * @param applications
     */
    private void assignIpAndIds(List<Application> applications){
        //assign unique id and ip to each application
        for(Application application : applications){
            if(application.getIp() == null){
                //generate new unique ip
                String ip = UniqueIPProvider.getInstance().getNextIPV4Address();

                // generate new unique id
                int id = UniqueIDProvider.getInstance().getNextID();
                UniqueIDProvider.getInstance().markIDused(id);

                application.setIp(ip);
                application.setId(id);
            } else {

                // generate only unique id as ip is already set in settings file.
                int id = UniqueIDProvider.getInstance().getNextID();
                UniqueIDProvider.getInstance().markIDused(id);

                application.setId(id);
            }
        }
    }
}
