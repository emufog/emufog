package emufog.topology;


public class Router extends Node{

    private Types.RouterType type;

    private int deviceCount;

    public Router(int asID) {
        super(asID);
        this.deviceCount = 0;
    }

    public Router(int id, int asID){
        super(id, asID);
        this.deviceCount = 0;
    }

    @Override
    public String getName() { return "r" + getID();}

    public void setType(Types.RouterType type) {
        this.type = type;
    }

    public Types.RouterType getType() {
        return type;
    }

    public boolean hasDevices(){ return deviceCount > 0;}

    public void incrementDeviceCount(int n){ deviceCount += n;}

    public void incrementDeviceCount() { deviceCount++;}

    public int getDeviceCount() { return deviceCount;}

}
