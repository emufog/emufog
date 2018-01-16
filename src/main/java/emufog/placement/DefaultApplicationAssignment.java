package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.FogNode;
import emufog.util.Logger;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
            deviceApplications = checkNotNull(Settings.getInstance().getDeviceApplications());
        } catch (Exception e) {
            e.printStackTrace(
            );
        }

        for(Application application : deviceApplications){
            Logger.getInstance().log(ReflectionToStringBuilder.toString(application, ToStringStyle.MULTI_LINE_STYLE));

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

        Logger logger = Logger.getInstance();
        logger.log(ReflectionToStringBuilder.toString(fogNodeList, ToStringStyle.MULTI_LINE_STYLE));

        //assign all fogNodeApplications to each fog node.
        for(FogNode fogNode : fogNodeList){
            fogNode.getConfiguration().setApplications(fogApplications);

            logger.log(ReflectionToStringBuilder.toString(fogNode, ToStringStyle.MULTI_LINE_STYLE));
        }

    }
}
