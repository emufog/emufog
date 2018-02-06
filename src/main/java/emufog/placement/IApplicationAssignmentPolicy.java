package emufog.placement;

import com.google.common.graph.MutableNetwork;

public interface IApplicationAssignmentPolicy {

    void generateDeviceApplicationMapping(MutableNetwork topology);

    void generateFogApplicationMapping(MutableNetwork topology);
}
