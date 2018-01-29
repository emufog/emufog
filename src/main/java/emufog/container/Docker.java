package emufog.container;

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

    private List<String> environment;

    private String image;

    private String imageVersion;

    private List<String> labels;

    private List<String> portBindings;

    /*List of volumes: ["/home/user1/:/mnt/vol2:rw"]*/
    private List<String> volumes;

    private boolean publishAllPorts;

    private String restartPolicy;


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

    public List<String> getEnvironment() {
        return environment;
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

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<String> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public boolean isPublishAllPorts() {
        return publishAllPorts;
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

    public List<String> getPortBindings() {
        return portBindings;
    }

    public void setPortBindings(List<String> portBindings) {
        this.portBindings = portBindings;
    }
}
