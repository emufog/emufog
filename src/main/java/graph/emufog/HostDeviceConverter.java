package graph.emufog;

/**
 * This class convert an existing node to a host device node in the graph.
 */
public class HostDeviceConverter extends NodeConverter {

    private final EmulationSettings emulationSettings;

    /**
     * Creates a new HostDeviceConverter instance to convert an existing node and
     * replace it with the given emulation settings.
     *
     * @param emulationSettings emulation settings of the newly created node
     * @throws IllegalArgumentException if the emulation settings object is null
     */
    public HostDeviceConverter(EmulationSettings emulationSettings) throws IllegalArgumentException {
        if (emulationSettings == null) {
            throw new IllegalArgumentException("The emulation settings object is not initialized.");
        }

        this.emulationSettings = emulationSettings;
    }

    @Override
    protected Node createNewNode(Node node) {
        return new HostDevice(node.id, node.as, emulationSettings);
    }

    @Override
    protected void addNodeToGraph(Node newNode) {
        newNode.as.addDevice((HostDevice) newNode);
    }

    @Override
    protected boolean needsConversion(Node node) {
        return !(node instanceof HostDevice);
    }

    @Override
    public HostDevice convert(Node node) {
        return (HostDevice) super.convert(node);
    }
}
