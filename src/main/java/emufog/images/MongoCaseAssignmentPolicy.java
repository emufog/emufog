package emufog.images;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import emufog.graph.Edge;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.graph.Switch;
import emufog.settings.Settings;

public class MongoCaseAssignmentPolicy implements IApplicationImageAssignmentPolicy {

	private final Map<String, List<String>> imageToCommand;
	
	public MongoCaseAssignmentPolicy() {
		imageToCommand = new HashMap<>();
		imageToCommand.put("mongo:latest", Arrays.asList("mongod --bind_ip_all --replSet rs0 &", "mongo 127.0.0.1:27017/admin --eval \"db.adminCommand({'replSetInitiate':EMUFOG_prepare_rs_init_json (hostips).replace ('\"', '\'')})\""));
		imageToCommand.put("ycsb:latest", null);
	}
	
	
	@Override
	public void generateImageMapping(Graph graph, Settings settings) {
		// TODO Auto-generated method stub
		Map<Integer, String> map = new HashMap<>();
		Collection<Switch> switches = graph.getSwitches();
		Switch mongoSwitch = null;
		float minAverage = Float.MAX_VALUE;
		for (Switch s: switches) {
			Edge[] connections = s.getEdges();
			float average = 0.0f;
			if (connections.length > 5) {
				for (Edge e: connections) {
					average += e.getDelay();
				}
				average /= connections.length;
				if (average < minAverage) {
					mongoSwitch = s;
					minAverage = average;
				}
			}
		}
		if (mongoSwitch != null) {
			PriorityQueue<Edge> edges = new PriorityQueue<>(11, Comparator.comparing(Edge::getDelay));
			edges.addAll(Arrays.asList(mongoSwitch.getEdges()));
			for (int i = 0; i < 5; i++) {
				Edge edge = edges.remove();
				Node assignTo = null;
				Node s = edge.getSource();
				Node d = edge.getDestination();
				if (s.equals(mongoSwitch)) {
					assignTo = d;
				} else {
					assignTo = s;
				}
				switch (i) {
				case 0:
					assignTo.setImage("mongo:latest");
					map.put(assignTo.getID(), "mongo:latest");
					break;
				case 1:
					assignTo.setImage("mongo:latest");
					map.put(assignTo.getID(), "mongo:latest");
					break;
				case 2:
					assignTo.setImage("mongo:latest");
					map.put(assignTo.getID(), "mongo:latest");
					break;
				case 3:
					assignTo.setImage("ycsb:latest");
					map.put(assignTo.getID(), "ycsb:latest");
					break;
				case 4:
					assignTo.setImage("debian:latest");
					map.put(assignTo.getID(), "debian:latest");
					break;
				}
			}
		}
		graph.setImages(map);
	}

	@Override
	public void generateCommandsLists(Graph graph, Settings settings) {
		// TODO Auto-generated method stub
		Map<Integer, List<String>> map = new HashMap<>();
		for (Node n: graph.getNodes()) {
			if (n.hasEmulationSettings() && (!n.getImage().equals(""))) {
				map.put(n.getID(), imageToCommand.get(n.getImage()));
			}
		}
		graph.setCommands(map);
	}

}
