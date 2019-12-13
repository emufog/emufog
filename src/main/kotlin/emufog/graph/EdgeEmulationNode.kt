package emufog.graph

import emufog.container.DeviceContainer

class EdgeEmulationNode(ip: String, deviceContainer: DeviceContainer) : EmulationNode(ip, deviceContainer) {

    override val container: DeviceContainer = deviceContainer
}