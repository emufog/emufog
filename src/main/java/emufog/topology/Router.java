package emufog.topology;


import java.util.concurrent.atomic.AtomicInteger;

public class Router extends Node {

    private Types.RouterType type;

    int connectedDevices = 0;

    AtomicInteger uncoveredDevices = new AtomicInteger();

    private AtomicInteger coveredDevices = new AtomicInteger();

    public Router(int asID) {
        super(asID);
        this.uncoveredDevices.set(0);
        this.coveredDevices.set(0);
    }

    public Router(int id, int asID) {
        super(id, asID);
        this.uncoveredDevices.set(0);
        this.coveredDevices.set(0);
    }

    @Override
    public String getName() {
        return "r" + getID();
    }

    public void setType(Types.RouterType type) {
        this.type = type;
    }

    public Types.RouterType getType() {
        return type;
    }

    public void addDevice(){
        connectedDevices++;
        uncoveredDevices.getAndIncrement();
    }

    public boolean hasDevices() {
        return connectedDevices > 0;
    }

    public int getUncoveredDevices() {
        return connectedDevices - coveredDevices.intValue();
    }

    public int connectedDevices(){
        return connectedDevices;
    }

    public void incrementCoveredCount() {
        coveredDevices.getAndIncrement();
    }

    public int coveredDevices(){
        return coveredDevices.intValue();
    }


    public boolean covered() {
        if (uncoveredDevices.intValue() == coveredDevices.intValue()) return true;
        return false;
    }

}
