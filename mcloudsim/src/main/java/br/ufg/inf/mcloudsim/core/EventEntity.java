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
 * Represents an event entity, which can be a publisher or a subscriber.
 * 
 * @author Raphael Gomes
 *
 */
public abstract class EventEntity {

	/** Counter to help identifier generation in subclasses **/
	protected static int idCont = 0;

	/** Entity identifier **/
	protected int id;
	
	/** Topic of entity events **/
	protected String topic;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
