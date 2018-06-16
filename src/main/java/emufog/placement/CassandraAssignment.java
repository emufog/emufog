package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.application.Application;
import emufog.container.Docker;
import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.FogNodeConfiguration;
import emufog.settings.Settings;
import emufog.topology.Device;
import emufog.topology.FogNode;
import emufog.util.Logger;
import emufog.util.UniqueIDProvider;
import emufog.util.UniqueIPProvider;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class CassandraAssignment implements IApplicationAssignmentPolicy{
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

        topology.nodes().stream().filter(n -> n instanceof Device).forEach(d -> deviceList.add(((Device) d)));

        //assign all deviceApplications to each device node
        for (Device device : deviceList) {
            for(Application application : deviceApplications){
                if(application.getName().equals("ycsb")){
                    Application ycsb = new Application(application, new Docker(application.getContainer()));
                    DeviceNodeConfiguration configuration = new DeviceNodeConfiguration();
                    configuration.addApplication(ycsb);
                    device.setConfiguration(configuration);
                }
            }
            assignIpAndIds(device.getConfiguration().getApplications().get(0));
        }

        addHosts();

        Logger logger = Logger.getInstance();
        int count = 0;
        for (Device device : deviceList) {
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


        /**
         * Assign cassandra container to node
         */

        for (FogNode fogNode : fogNodeList) {

            for (Application application : fogApplications) {
                if (application.getName().equals("cassandra")) {
                    Application cassandra = new Application(application, new Docker(application.getContainer()));
                    FogNodeConfiguration configuration = new FogNodeConfiguration();
                    configuration.addApplication(cassandra);
                    fogNode.setConfiguration(configuration);
                }
            }
            assignIpAndIds(fogNode.getConfiguration().getApplications().get(0));
        }

        addEnvironmentVariables();

        Logger logger = Logger.getInstance();
        int count = 0;
        for (FogNode fogNode : fogNodeList) {
            count += fogNode.getConfiguration().getApplications().size();
        }
        logger.log(String.format("Assigned %d applications to fog nodes\n", count));

    }

    /**
     * Assignees a unique id to each application and adds unique ip address if not already defined in the settings file.
     *
     * @param application
     */
    private void assignIpAndIds(Application application) {
        //assign unique id and ip to each application
        if (application.getIp() == null) {
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

    private void addHosts(){
        StringBuilder HOSTS = new StringBuilder();

        HOSTS.append("\"HOSTS=\\\"");

        for(int i=0; i < collectAddresses().size(); i++){
            if(i == collectAddresses().size() - 1 && i != 0){
                HOSTS.append(collectAddresses().get(i) + "\\\"\"");
            }else {
                HOSTS.append(collectAddresses().get(i) + " ");
            }

        }

        for(Device device : deviceList){
            for(Application deviceApplication : device.getConfiguration().getApplications()){
                if(deviceApplication.getName().equals("ycsb")){
                    deviceApplication.getContainer().addEnvironmentVariable(HOSTS.toString());
                }
            }
        }
    }

    private void addEnvironmentVariables(){

        StringBuilder CASSANDRA_SEEDS = new StringBuilder();

        CASSANDRA_SEEDS.append("\"CASSANDRA_SEEDS=");

        CASSANDRA_SEEDS.append(collectAddresses().get(0) + "\"");

        Logger.getInstance().log(CASSANDRA_SEEDS.toString());

        for(int i = 0; i < fogNodeList.size(); i++){
            StringBuilder IP = new StringBuilder().append("\"IP=");
            StringBuilder CQLSH_HOST = new StringBuilder().append("\"CQLSH_HOST=");
            StringBuilder SLEEP_TIME = new StringBuilder().append("\"SLEEP_TIME=");

            int timer = 0;

            timer = timer + (i * 60);

            for(Application application : fogNodeList.get(i).getConfiguration().getApplications()){
                if(application.getName().equals("cassandra")){
                    String nodeip = application.getIp();
                    IP.append(nodeip + "\"");
                    CQLSH_HOST.append(nodeip + "\"");
                    SLEEP_TIME.append(timer + "\"");
                    application.getContainer().addEnvironmentVariable(IP.toString());
                    application.getContainer().addEnvironmentVariable(CQLSH_HOST.toString());
                    application.getContainer().addEnvironmentVariable(SLEEP_TIME.toString());
                    application.getContainer().addEnvironmentVariable(CASSANDRA_SEEDS.toString());
                }
            }
        }
    }

    private List<String> collectAddresses() {

        List<String> adresses = new ArrayList();

        for (FogNode fogNode : fogNodeList) {

            for (Application application : fogNode.getConfiguration().getApplications()) {
                if (application.getName().equals("cassandra")) {
                    adresses.add(application.getIp());
                }
            }

        }

        return adresses;
    }
}
