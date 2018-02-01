package emufog.nodeconfig;

import emufog.application.Application;

import java.util.List;

/**
 * The NodeConfiguration contains the list of applications of a multi tier node.
 */
public abstract class NodeConfiguration {

    private List<Application> applications;

    //TODO: Discuss whether IP field on this level is reasonable.
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
