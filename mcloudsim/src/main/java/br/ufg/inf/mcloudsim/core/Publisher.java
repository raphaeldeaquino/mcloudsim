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
public class Publisher extends EdgeEventEntity {

	/** Rate of events generation **/
	private double rate;
	private double MIpr;
	private double Btr;

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
		super("P" + id, topic);
		this.rate = rate;
	}

	public Publisher(String id, String topic, double rate, double mIpr, double Btr) {
		super(id, topic);
		this.rate = rate;
		this.MIpr = mIpr;
		this.Btr = Btr;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getMIpr() {
		return MIpr;
	}

	public void setMIpr(double mIpr) {
		MIpr = mIpr;
	}

	public double getBtr() {
		return Btr;
	}

	public void setBtr(double Btr) {
		this.Btr = Btr;
	}

	@Override
	public void setId(String id) {
		if (id == null || id.isEmpty() || !id.startsWith("P"))
			throw new IllegalArgumentException("Invalid publisher ID: " + id);

		this.id = id;
	}

	@Override
	public String toString() {
		return "Publisher " + this.id;
	}

}
