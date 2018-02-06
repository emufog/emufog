package emufog.topology;

public class Link {

    private final int id;
    private final float delay;
    private final float bandwidth;

    public Link(int id, float delay, float bandwidth) {
        this.id = id;
        this.delay = delay;
        this.bandwidth = bandwidth;
    }

    public int getId() {
        return id;
    }

    public float getDelay() {
        return delay;
    }

    public float getBandwidth() {
        return bandwidth;
    }

}
