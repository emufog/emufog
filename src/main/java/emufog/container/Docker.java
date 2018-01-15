package emufog.container;

import java.util.List;

public class Docker extends Container{

    private int memoryLimit;
    private float cpuShare;

    private String containerName;
    private String entrypoint;
    private List<String> environment;
    private String image;
    private String imageVersion;
    private List<String> labels;
    private String ports;
    private List<String> volumes;
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
        this.cpuShare = cpuShare;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public float getCpuShare() {
        return cpuShare;
    }

    public void setCpuShare(float cpuShare) {
        this.cpuShare = cpuShare;
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

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
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
}
