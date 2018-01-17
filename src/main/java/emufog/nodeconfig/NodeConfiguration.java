package emufog.nodeconfig;

import emufog.application.Application;

import java.util.List;

public abstract class NodeConfiguration {

    private List<Application> applications;

    private String IP;

    public NodeConfiguration(String IP) {
        this.IP = IP;
    }

    public NodeConfiguration(){}

    public void setApplications(List<Application> applications) {

        this.applications = applications;

    }

    public List<Application> getApplications() {
        return applications;
    }

    public String getIP() {
        return IP;
    }
}
