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

    private List<String> environment = new ArrayList<>();

    private String image;

    private String imageVersion;

    private List<String> labels = new ArrayList<>();

    private List<String> ports = new ArrayList<>();

    /*List of volumes: ["/home/user1/:/mnt/vol2:rw"]*/
    private List<String> volumesList = new ArrayList<>();

    private boolean publishAllPorts = true;

    private List<String> dns = new ArrayList<>();

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
     * @return environment string in the format ["SOMEVARIABLE=xxx"].
     */
    public String getEnvironment() {

        StringBuilder environment = new StringBuilder();
        environment.append("[");

        if(this.environment.size() == 0){
            return environment.append("]").toString();
        } else {
            for(String env : this.environment){
                environment.append(env);
            }

            return environment.append("]").toString();
        }
    }

    public void setEnvironment(List<String> environment) {
        this.environment = environment;
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

        StringBuilder labels = new StringBuilder();
        labels.append("[");

        if(this.labels.size() == 0){
            return labels.append("]").toString();
        } else {
            for(String label : this.labels){
                labels.append(label);
            }

            return labels.append("]").toString();
        }

    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getVolumesList() {

        StringBuilder volumes = new StringBuilder();
        volumes.append("[");

        if(volumesList.size() == 0){
            return volumes.append("]").toString();
        } else {
            for(String volume : volumesList){
                volumes.append(volume);
            }

            return volumes.append("]").toString();
        }
    }

    public void setVolumesList(List<String> volumesList) {
        this.volumesList = volumesList;
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

    public String getPorts() {

        StringBuilder portbindings = new StringBuilder();

        portbindings.append("{");

        if(ports.size() == 0){
            return portbindings.append("}").toString();
        } else {
            for(String portbinding : ports){

                /*boolean lastItemInList = (portbindings.indexOf(portbinding) == (ports.size() -1));

                if(!lastItemInList){
                    portbindings.append(portbinding);
                    portbindings.append(",");
                } else {
                    portbindings.append(portbinding);

                }*/

                portbindings.append(portbinding);
            }

            return portbindings.append("}").toString();
        }
    }


    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
