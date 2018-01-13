package emufog.topology;

public class Router extends Node{

    private Types.RouterType type;

    private int deviceCount;

    public Router(int id, int asID) {
        super(id, asID);
        this.deviceCount = 0;
    }

    public void setType(Types.RouterType type) {
        this.type = type;
    }

    public Types.RouterType getType() {
        return type;
    }

    public boolean hasDevices(){ return deviceCount > 0;}

    public void incrementDeviceCount(int n){ deviceCount += n;}

    public int getDeviceCount() { return deviceCount;}



}
