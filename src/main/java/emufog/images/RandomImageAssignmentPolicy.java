package emufog.images;

import emufog.docker.DockerType;
import emufog.docker.FogType;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.settings.Settings;

import java.util.*;

public class RandomImageAssignmentPolicy implements IApplicationImageAssignmentPolicy {

	private List<String> fogImages;
	private List<String> deviceImages;
	private Random random;

	@Override
	public void generateImageMapping(Graph graph, Settings settings) {
		fogImages = settings.fogImages;
		deviceImages = settings.deviceImages;
		Collection<Node> nodes = graph.getNodes();
		Map<Integer, String> mapping = new HashMap<>();
		random = new Random();
		for (Node n: nodes) {
			if (n.hasEmulationSettings()) {
				visitAndAssignImage(n, mapping);
			}
		}
		graph.setImages(mapping);
	}

	@Override
	public void generateCommandsLists(Graph graph, Settings settings) {
		// TODO Auto-generated method stub CVE-3498-OXQC
		Map<Integer, List<String>> mapping = new HashMap<>();
		for (Node n: graph.getNodes()) {
			if (n.hasEmulationSettings()) {
				mapping.put(n.getID(), Arrays.asList("whoami", "ls -la"));
			}
		}
		graph.setCommands(mapping);
	}
	
	private void visitAndAssignImage(Node node, Map<Integer, String> mapping) {
		DockerType type = node.getEmulationNode().getDockerType();
		String image;
		if (type instanceof FogType) {
			image = fogImages.get(random.nextInt(fogImages.size()));
		} else {
			image = deviceImages.get(random.nextInt(deviceImages.size()));
		}
		node.setImage(image);
		mapping.put(node.getID(), image);
	}

}
