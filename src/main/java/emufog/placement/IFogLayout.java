package emufog.placement;

import com.google.common.graph.MutableNetwork;

public interface IFogLayout {

    void identifyFogNodes(MutableNetwork topology);

    void placeFogNodes(MutableNetwork topology);

}
