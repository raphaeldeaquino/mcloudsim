/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates an exponential distribution.
 * 
 * @author Raphael Gomes
 *
 */
public class Exp {

	/** The distribution rate **/
	private double rate;

	/** List of generated values **/
	private List<Double> values = new ArrayList<Double>();

	/**
	 * Creates an exponential distribution with the given rate.
	 * 
	 * @param rate
	 *            The distribution rate
	 */
	public Exp(double rate) {
		this.rate = rate;
		values.add(rate);
	}

	/**
	 * Generates next value in distribution.
	 *
	 * @return The next value in distribution
	 */
	public double next() {
		double next = -Math.log(Math.random()) / rate;
		values.add(next);
		return next;
	}

	/**
	 * Calculates the average of distribution values.
	 * 
	 * @return The average of generated values.
	 */
	public double average() {
		double sum = 0;
		for (Double d : values) {
			sum += d;
		}
		return sum / values.size();
	}
}
