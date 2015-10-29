/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.core;

/**
 * An entity that consumes events of broker.
 * 
 * @see EventEntity
 * @author Raphael Gomes
 *
 */
public class Subscriber extends EventEntity {

	/** Connectivity status of subscriber **/
	private ConnectivityStatus status;

	/**
	 * Creates a subscriber with automatic identifier and the given topic. The
	 * connectivity status is set to online.
	 * 
	 * @param id
	 *            The subscriber identifier
	 * @param topic
	 *            The subscriber topic
	 * @param status
	 *            The subscriber connectivity status
	 */
	public Subscriber(String topic) {
		this(++idCont, topic, ConnectivityStatus.ONLINE);
	}

	/**
	 * Creates a subscriber with the given identifier, topic. The connectivity
	 * status is set to online.
	 * 
	 * @param id
	 *            The subscriber identifier
	 * @param topic
	 *            The subscriber topic
	 */
	public Subscriber(int id, String topic) {
		this(id, topic, ConnectivityStatus.ONLINE);
	}

	/**
	 * Creates a subscriber with the given identifier, topic and connectivity
	 * status.
	 * 
	 * @param id
	 *            The subscriber identifier
	 * @param topic
	 *            The subscriber topic
	 * @param status
	 *            The subscriber connectivity status
	 */
	public Subscriber(int id, String topic, ConnectivityStatus status) {
		super();
		this.id = id;
		this.topic = topic;
		this.status = status;
	}

	public ConnectivityStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectivityStatus status) {
		this.status = status;
	}

}
