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
public class Subscriber extends EdgeEventEntity {

	/** Connectivity status of subscriber **/
	private ConnectivityStatus status;

	private double tON;
	private double tOFF;

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
		super("S" + id, topic);
		this.status = status;
	}

	public Subscriber(String id, String topic, double tON, double tOFF) {
		super(id, topic);
		this.tON = tON;
		this.tOFF = tOFF;
		this.status = ConnectivityStatus.ONLINE;
	}

	public ConnectivityStatus getStatus() {
		return status;
	}
	
	public boolean isOnline() {
		return this.status == ConnectivityStatus.ONLINE;
	}
	
	public boolean isOffline() {
		return this.status == ConnectivityStatus.OFFLINE;
	}

	public void setStatus(ConnectivityStatus status) {
		this.status = status;
	}

	public double gettON() {
		return tON;
	}

	public void settON(double tON) {
		this.tON = tON;
	}

	public double gettOFF() {
		return tOFF;
	}

	public void settOFF(double tOFF) {
		this.tOFF = tOFF;
	}

	@Override
	public String toString() {
		return "Subscriber " + id + " (" + getStatus() + ")";
	}

	@Override
	public void setId(String id) {
		if (id == null || id.isEmpty() || !id.startsWith("S"))
			throw new IllegalArgumentException("Invalid subscriber ID: " + id);

		this.id = id;
	}

}
