package emufog.topology;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AS {

    final int id;

    private int degree;

    private Map<Integer, Node> nodeMap;

    public AS(int id) {
        this.id = id;
        degree = 0;
        nodeMap = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public int getDegree() {
        return degree;
    }

    void incrementDegree(){ degree++;}


    void  addNode(Node node){
        nodeMap.put(node.getID(), node);
    }

    Node getNode(int id){
        return (Node) nodeMap.entrySet().stream().filter(node -> node.getKey() == id);
    }

    void removeNode(Node node){
        nodeMap.remove(node.getID(), node);
    }

    public Collection<Router> getRouters(){
        return (Collection<Router>) nodeMap.values().stream().filter(node -> node instanceof Router);
    }

    public Collection<Device> getDevices(){
        return (Collection<Device>) nodeMap.values().stream().filter(node -> node instanceof Device);
    }

    public Collection<FogNode> getFogNodes(){
        return (Collection<FogNode>) nodeMap.values().stream().filter(node -> node instanceof FogNode);
    }




}
