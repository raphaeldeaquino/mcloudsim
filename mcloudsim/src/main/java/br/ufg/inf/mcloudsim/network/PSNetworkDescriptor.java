package br.ufg.inf.mcloudsim.network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;

public class PSNetworkDescriptor {

	private Map<String, EventEntity> nodes;
	private List<PSNetworkPath> paths;

	public PSNetworkDescriptor() {
		this.nodes = new HashMap<>();
		this.paths = new LinkedList<>();
	}

	public void addNetworkNode(EventEntity networkNode) {
		if (networkNode == null || nodes.containsKey(networkNode.getId()))
			throw new IllegalArgumentException();

		nodes.put(networkNode.getId(), networkNode);
	}

	public EventEntity getNetworkNode(String id) {
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException();

		return nodes.get(id);
	}

	public void addNetworkPath(PSNetworkPath networkPath) {
		if (networkPath == null)
			throw new IllegalArgumentException();

		paths.add(networkPath);
	}

	public PSNetworkPath getNetworkPath(int number) {
		if (number < 0 || number >= this.paths.size())
			throw new IllegalArgumentException();

		return paths.get(number);
	}

	public PSNetworkPath getNetworkPathWithPublisher(String pubId) {
		for (PSNetworkPath networkPath : this.paths) {
			Publisher publisher = networkPath.getPublisher();
			if (publisher.getId().equals(pubId))
				return networkPath;
		}

		return null;
	}

	public PSNetworkPath getNetworkPathWithSubscriber(String subId) {
		for (PSNetworkPath networkPath : this.paths) {
			Subscriber subscriber = networkPath.getSubscriber();
			if (subscriber.getId().equals(subId))
				return networkPath;
		}

		return null;
	}

	public PSNetworkPath getNetworkPathWithTopic(String topic) {
		for (PSNetworkPath networkPath : this.paths) {
			Publisher publisher = networkPath.getPublisher();
			if (publisher.getTopic().equals(topic))
				return networkPath;
		}

		return null;
	}

	public List<PSNetworkPath> getPaths() {
		return paths;
	}

	@Override
	public String toString() {
		return "Network: Nodes=" + nodes.keySet() + "; Paths=" + paths;
	}

	/**
	 * Check if all nodes in a given path are included in the set
	 * 
	 * @param pathId
	 * @param nodes
	 * @return
	 */
	public boolean isComplete(String pathId, Set<String> nodes) {
		PSNetworkPath path = getNetworkPathWithPublisher(pathId);

		for (String node : nodes) {
			boolean found = false;
			for (DeployablePathNode pathNode : path.getBrokersPath()) {
				if (pathNode.getTargetNode().getId().equals(node)) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}

		return true;
	}
}
