package emufog.application;

import emufog.container.Container;
import emufog.container.Docker;

/**
 * Application object defining container, mounts, scripts.
 */
public class Application {

    private String name;

    private enum applicationType {
        DEVICE_APPLICATION, FOG_APPLICATION
    }

    Container container = new Docker();

    public void image(String img){
        container.image(img);
    }

    public void resources(int memoryLimit, float cpuShare){
        container.memoryLimit(memoryLimit);
        container.cpuShare(cpuShare);
    }


}
