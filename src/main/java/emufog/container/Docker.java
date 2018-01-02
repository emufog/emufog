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

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public void setCpuShare(float cpuShare) {
        this.cpuShare = cpuShare;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    public void setEnvironment(List<String> environment) {
        this.environment = environment;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }
}
