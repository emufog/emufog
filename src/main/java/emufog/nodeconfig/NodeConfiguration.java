package emufog.nodeconfig;

import emufog.application.Application;
import emufog.topology.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * The NodeConfiguration contains the list of applications of a multi tier node.
 */
public abstract class NodeConfiguration {

    private List<Application> applications = new LinkedList<>();

    public NodeConfiguration(){}

    public NodeConfiguration(NodeConfiguration configuration){
        this.applications = configuration.getApplications();
    }

    public void setApplications(List<Application> applications) {

        this.applications = applications;

    }

    public List<Application> getApplications() {
        return applications;
    }

    public void addApplication(Application application) {
        applications.add(application);
    }

}
