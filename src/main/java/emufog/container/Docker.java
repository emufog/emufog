package emufog.container;

import java.util.ArrayList;
import java.util.List;

public class Docker extends Container{

    //TODO: Implement Log config log_config(dict) docker-py - only possible if containernet is updated.

    /* mem_limit: Memory limit*/
    private int memoryLimit;

    /* Relative amount of max. avail CPU for container.
    * not a hard limit, e.g if only one container is busy the rest idle)
    * usage: d1=4 d2=6 <=> 40% 60% CPU*/
    private float cpuShares;

    /*the total available runtime within a period in 10^-6s*/
    private float cpuQuota;

    /*the length of a period in 10^-6s*/
    private float cpuPeriod;

    /*cpuset_cpus: Bind container to CPU 0 = cpu_1 ... n-1 = cpu_n (string: '0,2')*/
    private String cpuSet;

    private String containerName;

    private String entrypoint;

    /*List of environment variables*/
    private List<String> environment = new ArrayList<>();

    private String image;

    private String imageVersion;

    /*List of docker labels*/
    private List<String> labels = new ArrayList<>();

    /*List of ports*/
    private List<String> ports = new ArrayList<>();

    /*List of port bindings from container to hostmachine*/
    private List<String> portBindings = new ArrayList<>();

    /*List of volumes: ['/home/user1/:/mnt/vol2:rw']*/
    private List<String> volumes = new ArrayList<>();

    private boolean publishAllPorts = true;

    private List<String> dns = new ArrayList<>();

    /*List of commands*/
    private List<String> commands = new ArrayList<>();

    @Override
    public void image(String img) {
        this.image = img;
    }

    @Override
    public void imageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    @Override
    public void memoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    @Override
    public void cpuShare(float cpuShare) {
        this.cpuShares = cpuShare;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public float getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(float cpuShares) {
        this.cpuShares = cpuShares;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    /**
     * Return formatted environment string. The docker-py api is able to consume either dicts or lists.
     * Implemented is list formatting.
     * @return environment string in the format ['SOMEVARIABLE=xxx'].
     */
    public String getEnvironment() {

        StringBuilder environmentString = new StringBuilder();
        environmentString.append("[");

        if(this.environment.size() == 0){
            return environmentString.append("]").toString();
        } else {
            int size = environment.size();
            for(String env : environment){
                if(size != 1){
                    environmentString.append(env + ",");
                    size--;
                }else environmentString.append(env);

            }
            return environmentString.append("]").toString();
        }
    }

    public void setEnvironment(List<String> environment) {
        this.environment = environment;
    }

    /**
     * Method to add additional Environment Variables to the already existing list of environment variables.
     * @param envrionmentVariable to add to the list
     */
    public void addEnvironmentVariable(String envrionmentVariable){
        this.environment.add(envrionmentVariable);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    /**
     * Return formatted label string. The docker-py api is able to consume either dicts or lists.
     * Implemented is list formatting.
     * @return labels string in the format ["label1", "label2"]
     */
    public String getLabels() {

        StringBuilder labelsString = new StringBuilder();
        labelsString.append("[");

        if(this.labels.size() == 0){
            return labelsString.append("]").toString();
        } else {
            int size = labels.size();

            for(String label : this.labels){

                if(size != 1){
                    labelsString.append(label + ",");
                    size--;
                }else labelsString.append(label);

            }

            return labelsString.append("]").toString();
        }

    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     * Returns formatted volumes string. Implemented as python list syntax.
     * @return volumes string in the format ['/home/user1/:/mnt/vol2:rw', ... , /home/user1/:/mnt/vol2:rw]
     */
    public String getVolumes() {

        StringBuilder volumesString = new StringBuilder();
        volumesString.append("[");

        if(volumes.size() == 0){
            return volumesString.append("]").toString();
        } else {
            int size = volumes.size();

            for(String volume : volumes){
                if(size != 1){
                    volumesString.append(volume + ",");
                    size--;
                }else volumesString.append(volume);
            }

            return volumesString.append("]").toString();
        }
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public String isPublishAllPorts() {

        if(publishAllPorts){
            return "True";
        } else {
            return "False";
        }

    }

    public void setPublishAllPorts(boolean publishAllPorts) {
        this.publishAllPorts = publishAllPorts;
    }

    public String getCpuSet() {
        return cpuSet;
    }

    public void setCpuSet(String cpuSet) {
        this.cpuSet = cpuSet;
    }

    public float getCpuPeriod() {
        return cpuPeriod;
    }

    public void setCpuPeriod(float cpuPeriod) {
        this.cpuPeriod = cpuPeriod;
    }

    public float getCpuQuota() {
        return cpuQuota;
    }

    public void setCpuQuota(float cpuQuota) {
        this.cpuQuota = cpuQuota;
    }

    /**
     * Returns formatted ports sting. Implemented as python list syntax.
     * @return ports string in the format [8080,80, ... , 1235]
     */
    public String getPorts() {

        StringBuilder portString = new StringBuilder();

        portString.append("[");

        if(ports.size() == 0){
            return portString.append("]").toString();
        } else {
            int size = ports.size();
            for(String port : ports){
                if(size != 1) {
                    portString.append(port + ",");
                    size--;
                }else portString.append(port);
            }

            return portString.append("]").toString();
        }
    }


    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    /**
     * Add additional port to publish.
     * @param port
     */
    public void addPort(String port){this.ports.add(port);}

    public List<String> getCommands() {
        return commands;
    }

    /**
     * Method to add additional command to commands list.
     * @param command to add.
     */
    public void addCommand(String command){ this.commands.add(command);}

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    /**
     * Returns formatted port binding string implemented in as python dict syntax.
     * @return port binding string in the format {8080:8080, 80:80, ..., 1235:443}
     */
    public String getPortBindings() {

        StringBuilder portBindingString = new StringBuilder();

        portBindingString.append("{");

        if(portBindings.size() == 0){
            return portBindingString.append("}").toString();
        } else {
            int size = portBindings.size();
            for(String portBinding : portBindings){
                if(size != 1){
                    portBindingString.append(portBinding + ",");
                    size--;
                } else portBindingString.append(portBinding);
            }

            return portBindingString.append("}").toString();
        }


    }

    /**
     * Add additional port binding.
     * @param portBinding string in the format 8080:8080
     */
    public void addPortBinding(String portBinding){this.portBindings.add(portBinding);}

    public void setPortBindings(List<String> portBindings) {
        this.portBindings = portBindings;
    }
}
