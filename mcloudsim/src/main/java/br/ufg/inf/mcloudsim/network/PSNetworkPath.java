package br.ufg.inf.mcloudsim.network;

import java.util.LinkedList;

import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;

/**
 * For simplicity we assume that publishers and subscribers are part of a single
 * path
 * 
 * @author raphael
 *
 */
public class PSNetworkPath {

	private Publisher publisher;
	private Subscriber subscriber;
	private LinkedList<DeployablePathNode> brokersPath;

	public PSNetworkPath(Publisher publisher, Subscriber subscriber) {
		super();
		this.publisher = publisher;
		this.subscriber = subscriber;
		this.brokersPath = new LinkedList<>();
	}

	/**
	 * We use the publisher id as path id
	 * 
	 * @return
	 */
	public String getPathId() {
		return this.publisher.getId();
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	
	public void setBrokersPath(LinkedList<DeployablePathNode> brokersPath) {
		this.brokersPath = brokersPath;
	}

	public void addBrokerNode(DeployablePathNode brokerNode) {
		if (brokerNode == null)
			throw new IllegalArgumentException();

		this.brokersPath.add(brokerNode);
	}

	public LinkedList<DeployablePathNode> getBrokersPath() {
		return brokersPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((brokersPath == null) ? 0 : brokersPath.hashCode());
		result = prime * result + ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + ((subscriber == null) ? 0 : subscriber.hashCode());
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
		PSNetworkPath other = (PSNetworkPath) obj;
		if (brokersPath == null) {
			if (other.brokersPath != null)
				return false;
		} else if (!brokersPath.equals(other.brokersPath))
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (subscriber == null) {
			if (other.subscriber != null)
				return false;
		} else if (!subscriber.equals(other.subscriber))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Path: publisher=" + publisher + "; brokers=" + brokersPath + "; subscriber=" + subscriber;
	}

}
