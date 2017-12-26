/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a communication broker in publish/subscribe paradigm. In our model
 * we assume that there is just a single publisher to ever topic. Otherwise can
 * exist zero or several subscribers to each topic.
 * 
 * @author Raphael Gomes
 *
 */
public class PSNetworkRegistry {

	/** Publisher to ever topic **/
	private static Map<String, Publisher> pubMapByTopic = new HashMap<>();

	/** Subscriber to ever topic **/
	private static Map<String, Subscriber> subMapByTopic = new HashMap<>();
	
	/** Publisher to an ID **/
	private static Map<String, Publisher> pubMapById = new HashMap<>();

	/** List of subscribers to an ID **/
	private static Map<String, Subscriber> subMapById = new HashMap<>();

	/**
	 * Register a subscriber.
	 * 
	 * @param subscriber
	 *            The subscriber to be registered.
	 */
	public static void addSubscriber(Subscriber subscriber) {
		if (subscriber == null)
			throw new IllegalArgumentException();

		subMapByTopic.put(subscriber.getTopic(), subscriber);
		subMapByTopic.put(subscriber.getId(), subscriber);
	}
	
	public static Subscriber getSubscriberByTopic(String topic) {
		return subMapByTopic.get(topic);
	}
	
	public static Subscriber getSubscriberById(String id) {
		return subMapById.get(id);
	}
	
	public static ConnectivityStatus getConnectivityStatus(String subId) {
		if (!subMapById.containsKey(subId))
			throw new IllegalArgumentException();
		
		return subMapById.get(subId).getStatus();
	}
	
	/**
	 * Register a publisher.
	 * 
	 * @param publisher
	 *            The publisher to be registered.
	 * @throws IllegalArgumentException
	 *             if a previous topic was registered to the given topic.
	 */
	public static void addPublisher(Publisher publisher) {
		if (pubMapByTopic.containsKey(publisher.getTopic()))
			throw new IllegalArgumentException("Publisher already exists");

		pubMapByTopic.put(publisher.getTopic(), publisher);
		pubMapById.put(publisher.getId(), publisher);
	}

	

	public static Publisher getPublisherByTopic(String topic) {
		return pubMapByTopic.get(topic);
	}
	
	public static Publisher getPublisherById(String id) {
		return pubMapById.get(id);
	}

	/**
	 * Erase all previous registered publishers and subscribers.
	 */
	public static void reset() {
		pubMapByTopic = new HashMap<String, Publisher>();
		subMapByTopic = new HashMap<String, Subscriber>();
		pubMapById = new HashMap<String, Publisher>();
		subMapById = new HashMap<String, Subscriber>();
	}
}
