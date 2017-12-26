package br.ufg.inf.mcloudsim.network;

import br.ufg.inf.mcloudsim.core.CoreEventEntity;

public class DeployablePathNode extends PathNode {

	private double lOthers;

	public DeployablePathNode(CoreEventEntity targetNode, double lOthers) {
		this.targetNode = targetNode;
		this.lOthers = lOthers;
	}

	public double getlOthers() {
		return lOthers;
	}

	public void setlOthers(double lOthers) {
		this.lOthers = lOthers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(lOthers);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeployablePathNode other = (DeployablePathNode) obj;
		if (Double.doubleToLongBits(lOthers) != Double.doubleToLongBits(other.lOthers))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
