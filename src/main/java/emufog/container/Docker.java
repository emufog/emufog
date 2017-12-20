package emufog.container;

import java.util.List;

public class Docker extends Container{

    private int memoryLimit;
    private float cpuShare;

    private String containerName;
    private String entrypoint;
    private List<String> environment;
    private String image;
    private List<String> labels;
    private String ports;
    private List<String> volumes;
    private String restart;

    public void image(String image){
        this.image = image;
    }

    public void memoryLimit(int limit){
        this.memoryLimit = limit;
    }

    public void cpuShare(float share){
        this.cpuShare = share;
    }
}
