package emufog.nodeconfig;

import emufog.application.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * The NodeConfiguration contains the list of applications of a multi tier node.
 */
public abstract class NodeConfiguration {

    private List<Application> applications = new LinkedList<>();

    public NodeConfiguration(){}

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
