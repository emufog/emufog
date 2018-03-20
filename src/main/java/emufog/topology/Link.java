package emufog.topology;

import emufog.util.UniqueIDProvider;

import java.util.Comparator;

public class Link {

    private final int id;
    private final float delay;
    private final float bandwidth;

    public Link(int id, float delay, float bandwidth) {
        this.id = id;
        this.delay = delay;
        this.bandwidth = bandwidth;
    }

    public Link(float delay, float bandwidth){
        this.id = UniqueIDProvider.getInstance().getNextID();
        UniqueIDProvider.getInstance().markIDused(id);
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

    class LinkDelayComparator implements Comparator<Link> {

        @Override
        public int compare(Link link1, Link link2) {

            if(link1.delay < link2.delay) return -1;
            if(link1.delay > link2.delay) return 1;

            return 0;
        }
    }

    class LinkBandwidthComparator implements Comparator<Link> {

        @Override
        public int compare(Link link1, Link link2) {

            if(link1.bandwidth < link2.bandwidth) return -1;
            if(link1.bandwidth > link2.bandwidth) return 1;

            return 0;
        }
    }

}
