package emufog.application;

import emufog.container.Docker;

/**
 * Application object defining container, mounts, scripts.
 */
public class Application {

    private String name;

    private String IP;

    Docker container = new Docker();

    /**
     * Set corresponding application image.
     * @param img
     */
    public void image(String img, String imageVersion){
        container.image(img);
        container.imageVersion(imageVersion);
    }

    public void resources(int memoryLimit, float cpuShare){
        container.memoryLimit(memoryLimit);
        container.cpuShare(cpuShare);
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
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
}
