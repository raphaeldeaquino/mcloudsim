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
 * Represents an event entity, which can be a publisher, a subscriber, or a
 * broker.
 * 
 * @author Raphael Gomes
 *
 */
public abstract class EventEntity {

	/** Counter to help identifier generation in subclasses **/
	protected static int idCont = 0;

	/** Entity identifier **/
	protected String id;

	public EventEntity(String id) {
		super();
		setId(id);
	}

	public String getId() {
		return id;
	}

	public abstract void setId(String id);

	public static void reset() {
		idCont = 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		EventEntity other = (EventEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.id;
	}
}
