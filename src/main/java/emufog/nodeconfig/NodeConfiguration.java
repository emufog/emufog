package emufog.nodeconfig;

import emufog.application.Application;

import java.util.List;

public abstract class NodeConfiguration {

    private List<Application> applications;

    private String IP;

    public NodeConfiguration(String IP) {
        this.IP = IP;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public String getIP() {
        return IP;
    }
}
