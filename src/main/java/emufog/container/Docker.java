package emufog.container;

import java.util.ArrayList;
import java.util.List;

public class Docker extends Container{

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

    private List<String> environmentList = new ArrayList<>();

    private String image;

    private String imageVersion;

    private List<String> labelList = new ArrayList<>();

    private List<String> portBindings = new ArrayList<>();

    /*List of volumes: ["/home/user1/:/mnt/vol2:rw"]*/
    private List<String> volumesList = new ArrayList<>();

    private boolean publishAllPorts = true;

    private List<String> dns = new ArrayList<>();

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
     * Return formated environment string.
     * @return
     */
    public String getEnvironment() {
        String environment = "{";

        if(environmentList.size() == 0){
            return environment + "}";
        } else {
            for(String env : environmentList){
                environment = environment + env;
            }

            return environment + "}";
        }
    }

    public void setEnvironment(List<String> environment) {
        this.environmentList = environment;
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

    public String getLabelList() {
        String labels = "[";

        if(labelList.size() == 0){
            return labels + "]";
        } else {
            for(String label : labelList){
                labels = labels + label;
            }

            return labels + "]";
        }

    }

    public void setLabelList(List<String> labelList) {
        this.labelList = labelList;
    }

    public String getVolumesList() {
        String volumes = "[";

        if(volumesList.size() == 0){
            return volumes + "]";
        } else {
            for(String volume : volumesList){
                volumes = volumes + volume;
            }

            return volumes + "]";
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

    public String getPortBindings() {

        StringBuilder portbindings = new StringBuilder();

        portbindings.append("{");

        if(portBindings.size() == 0){
            return portbindings.append("}").toString();
        } else {
            for(String portbinding : portBindings){

                /*boolean lastItemInList = (portbindings.indexOf(portbinding) == (portBindings.size() -1));

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


    public void setPortBindings(List<String> portBindings) {
        this.portBindings = portBindings;
    }
}
