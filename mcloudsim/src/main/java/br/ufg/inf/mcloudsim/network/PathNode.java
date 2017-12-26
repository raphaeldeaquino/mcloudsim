package br.ufg.inf.mcloudsim.network;

import br.ufg.inf.mcloudsim.core.CoreEventEntity;
import br.ufg.inf.mcloudsim.core.EdgeEventEntity;
import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;

public abstract class PathNode {

	protected EventEntity targetNode;

	public EventEntity getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(EventEntity targetNode) {
		this.targetNode = targetNode;
	}

	public boolean isDeployableNode() {
		return this.targetNode instanceof CoreEventEntity;
	}

	public boolean isLegacyNode() {
		return this.targetNode instanceof EdgeEventEntity;
	}

	public boolean isPublisherNode() {
		return this.targetNode instanceof Publisher;
	}

	public boolean isSubscriberNode() {
		return this.targetNode instanceof Subscriber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((targetNode == null) ? 0 : targetNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathNode other = (PathNode) obj;
		if (targetNode == null) {
			if (other.targetNode != null)
				return false;
		} else if (!targetNode.equals(other.targetNode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return targetNode.toString();
	}

}
