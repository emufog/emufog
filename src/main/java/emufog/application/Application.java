package emufog.application;

import emufog.container.Docker;

/**
 * Application object defining container, mounts, scripts.
 */
public class Application {

    private String name;

    private int id;

    private String ip;

    Docker container = new Docker();

    /**
     * Set corresponding application image.
     * @param img imagename as String
     * @param imageVersion as String
     */
    public void image(String img, String imageVersion){
        container.image(img);
        container.imageVersion(imageVersion);
    }

    public void resources(int memoryLimit, float cpuShare){
        container.memoryLimit(memoryLimit);
        container.cpuShare(cpuShare);
    }

    public void setName(String name) {

        this.name = name;
        container.setContainerName(name);
    }

    public Docker getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
