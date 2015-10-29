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
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a communication broker in publish/subscribe paradigm. In our model
 * we assume that there is just a single publisher to ever topic. Otherwise can
 * exist zero or several subscribers to each topic.
 * 
 * @author Raphael Gomes
 *
 */
public class PSBroker {

	/** Publisher to ever topic **/
	private static Map<String, Publisher> pubMap = new TreeMap<String, Publisher>();

	/** List of subscribers to ever topic **/
	private static Map<String, List<Subscriber>> subMap = new TreeMap<String, List<Subscriber>>();

	/**
	 * Register a publisher.
	 * 
	 * @param publisher
	 *            The publisher to be registered.
	 * @throws IllegalArgumentException
	 *             if a previous topic was registered to the given topic.
	 */
	public static void addPublisher(Publisher publisher) {
		if (pubMap.containsKey(publisher.getTopic()))
			throw new IllegalArgumentException("Publisher already exists");

		pubMap.put(publisher.getTopic(), publisher);
	}

	/**
	 * Register a subscriber.
	 * 
	 * @param subscriber
	 *            The subscriber to be registered.
	 */
	public static void addSubscriber(Subscriber subscriber) {
		if (!subMap.containsKey(subscriber.getTopic()))
			subMap.put(subscriber.getTopic(), new ArrayList<Subscriber>());

		subMap.get(subscriber.getTopic()).add(subscriber);
	}

	public static Publisher getPublish(String topic) {
		return pubMap.get(topic);
	}

	public static List<Subscriber> getSubscribers(String topic) {
		return subMap.get(topic);
	}

	/**
	 * Erase all previous registered publishers and subscribers.
	 */
	public static void resetBroker() {
		pubMap = new TreeMap<String, Publisher>();
		subMap = new TreeMap<String, List<Subscriber>>();
	}
}
