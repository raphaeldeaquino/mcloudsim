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
 * An entity that generates events to broker.
 * 
 * @see EventEntity
 * @author Raphael Gomes
 *
 */
public class Publisher extends EventEntity {

	/** Rate of events generation **/
	private double rate;

	/**
	 * Creates a publisher with automatic identifier and given topic and rate.
	 * 
	 * @param topic
	 *            The publisher topic
	 * @param rate
	 *            Rate of events generation
	 */
	public Publisher(String topic, double rate) {
		this(++idCont, topic, rate);
	}

	/**
	 * Creates a publisher with the given identifier, topic and rate.
	 * 
	 * @param id
	 *            The publisher identifier
	 * @param topic
	 *            The publisher topic
	 * @param rate
	 *            Rate of events generation
	 */
	public Publisher(int id, String topic, double rate) {
		super();
		this.id = id;
		this.topic = topic;
		this.rate = rate;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

}
