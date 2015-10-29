/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import br.ufg.inf.mcloudsim.core.ConnectivityStatus;
import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.Exp;
import br.ufg.inf.mcloudsim.core.PSBroker;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.experiment.ExperimentConstants;
import br.ufg.inf.mcloudsim.experiment.PSExperiment;
import br.ufg.inf.mcloudsim.simulator.PSCloudlet.cloudletType;

/**
 * A broker acting on behalf of user to manage resources.<br>
 * It's the broker of resources management and can not be confused with Pub/Sub
 * broker.
 * 
 * @author Raphael Gomes
 *
 */
public class PSDatacenterBroker extends DatacenterBroker {

	/** Constants to event control */
	public static final int PUT_SUBSCRIBER_ONLINE = 50;
	public static final int PUT_SUBSCRIBER_OFFLINE = 51;

	/** Average measurements */
	protected static double avgRT;
	protected static double avgtON;
	protected static double avgtOFF;

	/** Maps each cloudlet to the specific entity */
	protected static Map<PSCloudlet, EventEntity> cloudletMap;

	/** Maps each publish cloudlet to the specific subscribe cloudlets */
	protected static Map<PSCloudlet, List<PSCloudlet>> cloudletPubMap;

	public PSDatacenterBroker(String name) throws Exception {
		super(name);
		cloudletMap = new TreeMap<PSCloudlet, EventEntity>();
		cloudletPubMap = new TreeMap<PSCloudlet, List<PSCloudlet>>();
		avgRT = avgtON = avgtOFF = 0.0;
	}

	public static double getAvgRT() {
		return avgRT;
	}

	public static double getAvgtON() {
		return avgtON;
	}

	public static double getAvgtOFF() {
		return avgtOFF;
	}

	public static Map<PSCloudlet, EventEntity> getCloudletMap() {
		return cloudletMap;
	}

	public static Map<PSCloudlet, List<PSCloudlet>> getCloudletPubMap() {
		return cloudletPubMap;
	}

	/**
	 * Submit publish cloudlets to the created VMs.<BR>
	 * It also schedules the subscriber ON/OFF behaviour.
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed
			// yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			cloudlet.setVmId(vm.getId());

			// The cloudlet starts its execution only after data transfer
			double startTime = ExperimentConstants.LATENCY_OVERHEAD + ((PSCloudlet) cloudlet).getArrivalTime()
					+ CloudSim.clock();
			send(getVmsToDatacentersMap().get(vm.getId()), startTime - CloudSim.clock(), CloudSimTags.CLOUDLET_SUBMIT,
					cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ ((PSCloudlet) cloudlet).getType() + " " + cloudlet.getCloudletId() + " to VM #" + vm.getId()
					+ " on time " + (startTime - CloudSim.clock()));
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}

		scheduleSub();
	}

	/**
	 * Schedule subscriber change of status (ONLINE or OFFLINE) according with
	 * an exponential distribution.
	 */
	protected void scheduleSub() {
		Exp distON = new Exp(ExperimentConstants.SUBSCRIBE_ON_OFF_RATE);
		Exp distOFF = new Exp(ExperimentConstants.SUBSCRIBE_ON_OFF_RATE);

		double offStart = distOFF.next();
		while (offStart <= ExperimentConstants.SIMULATION_INTERVAL) {
			send(getId(), offStart, PSDatacenterBroker.PUT_SUBSCRIBER_OFFLINE,
					PSBroker.getSubscribers(ExperimentConstants.TOPIC).get(0));

			double offEnd = offStart + distON.next();
			send(getId(), offEnd, PSDatacenterBroker.PUT_SUBSCRIBER_ONLINE,
					PSBroker.getSubscribers(ExperimentConstants.TOPIC).get(0));

			offStart = offEnd + distOFF.next();
		}

		avgtON = distON.average();
		avgtOFF = distOFF.average();
	}

	/**
	 * Process a cloudlet return event.<BR>
	 * If the finished cloudlet is a publish event, it schedules another to each
	 * subscriber.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processCloudletReturn(SimEvent ev) {
		PSCloudlet cloudlet = (PSCloudlet) ev.getData();

		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getType() + " "
				+ cloudlet.getCloudletId() + " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all
																		// cloudlets
																		// executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}

		// if a publish (ON or OFF) task is finished we need to generate a
		// subscribe task
		if (cloudlet.getType() == PSCloudlet.cloudletType.PUBLISH) {
			Publisher pub = (Publisher) cloudletMap.get(cloudlet);
			List<Subscriber> subs = PSBroker.getSubscribers(pub.getTopic());

			for (Subscriber s : subs) {
				PSCloudlet subCloudlet = new PSCloudlet(++PSExperiment.cloudletCount,
						(long) (ExperimentConstants.SERVICE_TIME_OUT * ExperimentConstants.BROKER_VM_MIPS), 1,
						ExperimentConstants.SUBSCRIBE_CLOUDLET_FILESIZE, ExperimentConstants.CLOUDLET_OUTPUT_SIZE,
						new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(),
						PSCloudlet.cloudletType.SUBSCRIBE, 0);
				cloudletMap.put(subCloudlet, s);
				cloudletPubMap.get(cloudlet).add(subCloudlet);

				getCloudletList().add(subCloudlet);
				subCloudlet.setVmId(cloudlet.getVmId());
				subCloudlet.setUserId(getId());
				Vm vm = getVmsCreatedList().get(cloudlet.getVmId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, subCloudlet);
				cloudletsSubmitted++;
				getCloudletSubmittedList().add(subCloudlet);

				Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending subscribe cloudlet "
						+ subCloudlet.getCloudletId() + " regarding publish cloudlet " + cloudlet.getType() + " "
						+ cloudlet.getCloudletId());
			}

		}
	}

	/**
	 * Treat events of change subscriber status (ONLINE and OFFLINE), according
	 * with distribution scheduled <i>a priori</i>.
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		int tag = ev.getTag();
		switch (tag) {
		case PUT_SUBSCRIBER_ONLINE:
			Subscriber subscriber = (Subscriber) ev.getData();
			subscriber.setStatus(ConnectivityStatus.ONLINE);
			Log.printLine(
					CloudSim.clock() + ": " + getName() + ": Putting subscriber " + subscriber.getId() + " online");
			break;
		case PUT_SUBSCRIBER_OFFLINE:
			subscriber = (Subscriber) ev.getData();
			subscriber.setStatus(ConnectivityStatus.OFFLINE);
			Log.printLine(
					CloudSim.clock() + ": " + getName() + ": Putting subscriber " + subscriber.getId() + " offline");
			break;
		default:
			throw new UnsupportedOperationException("Unknow event type");
		}
	}

	/**
	 * Print in the standard log the simulation results.<BR>
	 * It must be called. Otherwise {@link DatacenterBroker#finishExecution}
	 * must be explicitly called.
	 */
	public void printRT() {
		// Calculate and print the response time
		Log.printLine("========== RESPONSE TIME ==========");
		Log.printLine("Cloudlet ID" + "\t" + "Submission Time" + "\t" + "Response Time");

		Set<PSCloudlet> cloudlets = cloudletPubMap.keySet();

		// Calculates the RT according with publish arrival time and subscriber
		// average finish time
		double avgSum = 0.0;
		int avgCount = 0;
		for (PSCloudlet cloudlet : cloudlets) {
			if (cloudlet.getType() == cloudletType.PUBLISH) {
				double submissionTime = cloudlet.getSubmissionTime();
				double rtSum = 0.0;
				double rt = 0.0;
				int subCount = 0;

				// Calculate the average according with number of subscribers
				for (PSCloudlet subCloudlet : cloudletPubMap.get(cloudlet)) {
					rtSum += subCloudlet.getFinishTime() - submissionTime;
					subCount++;
				}
				rt = rtSum / subCount;
				Log.printLine(cloudlet.getCloudletId() + "\t" + submissionTime + "\t" + rt);

				// This should'nt be necessary
				if (rt >= 0.0 && rt != Double.NaN) {
					avgSum += rt;
					avgCount++;
				}
			}
		}

		avgRT = avgSum / avgCount;

		Log.printLine("Average RT = " + avgRT);
		Log.printLine("tON = " + avgtON);
		Log.printLine("tOFF = " + avgtOFF);

		super.finishExecution();
	}

}
