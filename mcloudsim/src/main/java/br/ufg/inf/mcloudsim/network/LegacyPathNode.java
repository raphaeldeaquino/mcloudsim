package br.ufg.inf.mcloudsim.network;

import br.ufg.inf.mcloudsim.core.EdgeEventEntity;

public class LegacyPathNode extends PathNode {

	private double rt;

	public LegacyPathNode(EdgeEventEntity targetNode, double rt) {
		this.targetNode = targetNode;
		this.rt = rt;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(rt);
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
		LegacyPathNode other = (LegacyPathNode) obj;
		if (Double.doubleToLongBits(rt) != Double.doubleToLongBits(other.rt))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
