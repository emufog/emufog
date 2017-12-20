package emufog.container;


public abstract class Container {

    public abstract void image(String img);

    public abstract void memoryLimit(int memoryLimit);

    public abstract void cpuShare(float cpuShare);
}
