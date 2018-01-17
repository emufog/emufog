package emufog.nodeconfig;

import emufog.application.Application;
import emufog.util.UniqueIPProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeConfiguration {

    private List<Application> applications;

    private String IP;

    public NodeConfiguration(String IP) {
        this.IP = IP;
    }

    //TODO: Implement clean IP assignment solution. And remove empy constructors for node configurations.
    // Workaround to be able to assign IP on application level.
    public NodeConfiguration(){

    }

    public void setApplications(List<Application> applications) {

        this.applications = new ArrayList<>();

        // assign ip for each application before application is added to list.
        for(Application application : applications){
            application.setIP(UniqueIPProvider.getInstance().getNextIPV4Address());
            this.applications.add(application);
        }

    }

    public List<Application> getApplications() {
        return applications;
    }

    public String getIP() {
        return IP;
    }
}
