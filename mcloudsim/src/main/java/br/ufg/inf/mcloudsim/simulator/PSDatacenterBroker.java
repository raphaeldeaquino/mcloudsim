/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import br.ufg.inf.mcloudsim.core.ConnectivityStatus;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.network.DeployablePathNode;
import br.ufg.inf.mcloudsim.network.PSNetworkDescriptor;
import br.ufg.inf.mcloudsim.network.PSNetworkPath;
import br.ufg.inf.mcloudsim.utils.Pair;

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

	/** Average RT for each broker in a given path */
	private Map<Pair<String, String>, Double> avgBrokerRTInPath;
	private Map<Pair<String, String>, Integer> finishedTasksPerBrokerInPath;

	/** Average input service demand for each broker in a given path */
	private Map<Pair<String, String>, Double> avgBrokerDPrInPath;
	private Map<Pair<String, String>, Integer> finishedInTasksPerBrokerInPath;

	/** Average input service demand for each broker in a given path */
	private Map<Pair<String, String>, Double> avgBrokerDTrInPath;
	private Map<Pair<String, String>, Integer> finishedOutTasksPerBrokerInPath;

	/** Average tON of each subscriber */
	private Map<String, Double> avgtON;

	/** Average tON of each subscriber */
	private Map<String, Double> avgtOFF;

	private Map<PSCloudlet, PSCloudlet> outToInTasksMap;

	private PSNetworkDescriptor psNetwork;

	private BiMap<String, Integer> nodeToVmMap;

	/** Controls the forwarding of messages */
	private Map<String, LinkedList<DeployablePathNode>> pathBrokersMap;

	public PSDatacenterBroker(String name, PSNetworkDescriptor psNetwork) throws Exception {
		super(name);
		this.avgBrokerRTInPath = new HashMap<>();
		this.finishedTasksPerBrokerInPath = new HashMap<>();
		this.avgBrokerDPrInPath = new HashMap<>();
		this.finishedInTasksPerBrokerInPath = new HashMap<>();
		this.avgBrokerDTrInPath = new HashMap<>();
		this.finishedOutTasksPerBrokerInPath = new HashMap<>();
		this.avgtON = new HashMap<>();
		this.avgtOFF = new HashMap<>();
		this.outToInTasksMap = new HashMap<>();
		this.psNetwork = psNetwork;
		this.nodeToVmMap = HashBiMap.create();
		this.pathBrokersMap = new HashMap<>();
	}

	public Map<String, Double> getAvgRTMapOfPath(String pathId) {
		Map<String, Double> avgRTOfPath = new HashMap<>();

		for (Pair<String, String> pair : this.avgBrokerRTInPath.keySet()) {
			if (pair.getLeft().equals(pathId))
				avgRTOfPath.put(pair.getRight(), this.avgBrokerRTInPath.get(pair));
		}

		return avgRTOfPath;
	}

	public double getAvgRTOfPath(String pathId) {
		Map<String, Double> avgRTOfPath = getAvgRTMapOfPath(pathId);
		double sum = 0.0;

		if (!this.psNetwork.isComplete(pathId, avgRTOfPath.keySet()))
			return Double.NaN;

		for (Double rt : avgRTOfPath.values())
			sum += rt;

		if (sum == 0.0)
			return Double.NaN;
		else
			return sum;
	}

	public Map<String, Double> getAvgDPrMapOfPath(String pathId) {
		Map<String, Double> avgDPrOfPath = new HashMap<>();

		for (Pair<String, String> pair : this.avgBrokerDPrInPath.keySet()) {
			if (pair.getLeft().equals(pathId))
				avgDPrOfPath.put(pair.getRight(), this.avgBrokerDPrInPath.get(pair));
		}

		return avgDPrOfPath;
	}

	public Map<String, Double> getAvgDTrMapOfPath(String pathId) {
		Map<String, Double> avgDTrOfPath = new HashMap<>();

		for (Pair<String, String> pair : this.avgBrokerDTrInPath.keySet()) {
			if (pair.getLeft().equals(pathId))
				avgDTrOfPath.put(pair.getRight(), this.avgBrokerDTrInPath.get(pair));
		}

		return avgDTrOfPath;
	}

	public Double getAvgTOnOfPath(String pathId) {
		return this.avgtON.get(pathId);
	}

	public Double getAvgTOFFOfPath(String pathId) {
		return this.avgtOFF.get(pathId);
	}

	/**
	 * It is used to set which VM has been created for each broker
	 * 
	 * @param brokerId
	 * @param vmId
	 */
	public void addVmMap(String brokerId, Integer vmId) {
		if (brokerId == null || brokerId.isEmpty())
			throw new IllegalArgumentException("Invalid node ID");

		if (vmId == null)
			throw new IllegalArgumentException("Invalid VM ID");

		if (this.nodeToVmMap.containsKey(brokerId))
			throw new IllegalArgumentException("Node " + brokerId + " already mapped");

		this.nodeToVmMap.put(brokerId, vmId);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @see #submitCloudletList(java.util.List)
	 */
	@Override
	protected void submitCloudlets() {
		List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();

		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			if (vm == null) { // vm was not created
				if (!Log.isDisabled()) {
					Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
							cloudlet.getCloudletId(), ": bount VM not available");
				}
				continue;
			}

			if (!Log.isDisabled()) {
				Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Scheduling input cloudlet ",
						cloudlet.getCloudletId(), " to VM #", vm.getId(), " on ",
						((PSCloudlet) cloudlet).getArrivalTime());
			}

			// The cloudlet starts its execution only after data transfer
			send(getVmsToDatacentersMap().get(vm.getId()), ((PSCloudlet) cloudlet).getArrivalTime() - CloudSim.clock(),
					CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
			successfullySubmitted.add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);

		initPathBrokersMap();
		scheduleIntermittence();
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
		int cloudletVm = cloudlet.getVmId();
		String currentNodeId = this.nodeToVmMap.inverse().get(cloudletVm);
		String pathId = cloudlet.getPathId();
		PSNetworkPath networkPath = this.psNetwork.getNetworkPathWithPublisher(cloudlet.getPathId());
		Publisher publisher = networkPath.getPublisher();

		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet + " received on broker "
				+ cloudlet.getBrokerId());
		cloudletsSubmitted--;

		// if an input task is finished we need to generate an output task
		if (cloudlet.isInput()) {
			PSCloudlet outCloudlet = new PSCloudlet(++PSSimulation.cloudletCount, pathId, cloudlet.getBrokerId(),
					(long) publisher.getMItr(), 1, SimulationConstants.SUBSCRIBE_CLOUDLET_FILESIZE,
					SimulationConstants.CLOUDLET_OUTPUT_SIZE, new UtilizationModelFull(), new UtilizationModelFull(),
					new UtilizationModelFull(), PSCloudlet.cloudletType.OUT, CloudSim.clock());

			getCloudletList().add(outCloudlet);
			outCloudlet.setVmId(cloudlet.getVmId());
			outCloudlet.setUserId(getId());
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(outCloudlet);
			sendNow(getVmsToDatacentersMap().get(outCloudlet.getVmId()), CloudSimTags.CLOUDLET_SUBMIT, outCloudlet);
			this.outToInTasksMap.put(outCloudlet, cloudlet);

			double DPr = cloudlet.getActualCPUTime();
			Pair<String, String> mapKey = Pair.of(cloudlet.getPathId(), cloudlet.getBrokerId());
			if (!this.avgBrokerDPrInPath.containsKey(mapKey)) {
				this.avgBrokerDPrInPath.put(mapKey, DPr);
				this.finishedInTasksPerBrokerInPath.put(mapKey, 1);
			} else {
				int finishedTasks = this.finishedInTasksPerBrokerInPath.get(mapKey) + 1;
				double avgDPr = (this.avgBrokerDPrInPath.get(mapKey) * (double) (finishedTasks - 1) + DPr)
						/ (double) finishedTasks;
				this.avgBrokerDPrInPath.put(mapKey, avgDPr);
				this.finishedInTasksPerBrokerInPath.put(mapKey, finishedTasks);
			}
		} else {
			PSCloudlet inCloudlet = this.outToInTasksMap.remove(cloudlet);
			DeployablePathNode nextBrokerNode = getNextBrokerInPath(pathId, currentNodeId);
			double DTr = cloudlet.getActualCPUTime();
			double rt = cloudlet.getFinishTime() - inCloudlet.getSubmissionTime();
			Pair<String, String> mapKey = Pair.of(cloudlet.getPathId(), cloudlet.getBrokerId());
			Log.printLine(
					CloudSim.clock() + ": " + getName() + ": RT for cloudlet " + cloudlet.getCloudletId() + " = " + rt);

			// Updates statistics
			if (!this.avgBrokerDTrInPath.containsKey(mapKey)) {
				this.avgBrokerDTrInPath.put(mapKey, DTr);
				this.finishedOutTasksPerBrokerInPath.put(mapKey, 1);
			} else {
				int finishedTasks = this.finishedOutTasksPerBrokerInPath.get(mapKey) + 1;
				double avgDTr = (this.avgBrokerDTrInPath.get(mapKey) * (double) (finishedTasks - 1) + DTr)
						/ (double) finishedTasks;
				this.avgBrokerDTrInPath.put(mapKey, avgDTr);
				this.finishedOutTasksPerBrokerInPath.put(mapKey, finishedTasks);
			}
			if (!this.avgBrokerRTInPath.containsKey(mapKey)) {
				this.avgBrokerRTInPath.put(mapKey, rt);
				this.finishedTasksPerBrokerInPath.put(mapKey, 1);
			} else {
				int finishedTasks = this.finishedTasksPerBrokerInPath.get(mapKey) + 1;
				double avgRT = (this.avgBrokerRTInPath.get(mapKey) * (double) (finishedTasks - 1) + rt)
						/ (double) finishedTasks;
				this.avgBrokerRTInPath.put(mapKey, avgRT);
				this.finishedTasksPerBrokerInPath.put(mapKey, finishedTasks);
			}

			// if there is another broker we need to generate an input task
			if (nextBrokerNode != null) {
				int nextVmId = this.nodeToVmMap.get(nextBrokerNode.getTargetNode().getId());

				PSCloudlet nextInCloudlet = new PSCloudlet(++PSSimulation.cloudletCount, pathId,
						nextBrokerNode.getTargetNode().getId(), (long) publisher.getMIpr(), 1,
						SimulationConstants.SUBSCRIBE_CLOUDLET_FILESIZE, SimulationConstants.CLOUDLET_OUTPUT_SIZE,
						new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(),
						PSCloudlet.cloudletType.IN, CloudSim.clock());

				getCloudletList().add(nextInCloudlet);
				nextInCloudlet.setVmId(nextVmId);
				nextInCloudlet.setUserId(getId());
				cloudletsSubmitted++;
				getCloudletSubmittedList().add(nextInCloudlet);
				sendNow(getVmsToDatacentersMap().get(nextInCloudlet.getVmId()), CloudSimTags.CLOUDLET_SUBMIT,
						nextInCloudlet);
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
					CloudSim.clock() + ": " + getName() + ": Putting subscriber " + subscriber.getId() + " ONLINE");
			resumePausedCloudlets(subscriber.getId());
			startNewCloudlets(subscriber.getId());
			break;
		case PUT_SUBSCRIBER_OFFLINE:
			subscriber = (Subscriber) ev.getData();
			subscriber.setStatus(ConnectivityStatus.OFFLINE);
			Log.printLine(
					CloudSim.clock() + ": " + getName() + ": Putting subscriber " + subscriber.getId() + " OFFLINE");
			break;
		default:
			throw new UnsupportedOperationException("Unknow event type");
		}
	}

	private void initPathBrokersMap() {
		List<PSNetworkPath> paths = this.psNetwork.getPaths();

		for (PSNetworkPath path : paths) {
			LinkedList<DeployablePathNode> pathBrokers = path.getBrokersPath();
			String pathId = path.getPathId();

			this.pathBrokersMap.put(pathId, pathBrokers);
		}
	}

	/**
	 * Schedule subscriber change of status (ONLINE or OFFLINE) according with
	 * an exponential distribution.
	 */
	private void scheduleIntermittence() {
		List<PSNetworkPath> networkPaths = this.psNetwork.getPaths();

		for (PSNetworkPath networkPath : networkPaths) {
			Subscriber subscriber = networkPath.getSubscriber();
			double onRate = subscriber.gettON();
			double offRate = subscriber.gettOFF();

			if (onRate > 0.0 && offRate > 0.0) {
				ExponentialDistribution distON = new ExponentialDistribution(onRate);
				ExponentialDistribution distOFF = new ExponentialDistribution(offRate);

				double offStart = distON.sample();
				while (offStart <= SimulationConstants.SIMULATION_INTERVAL) {
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Subscriber scheduled to go offline at "
							+ offStart);
					send(getId(), offStart, PSDatacenterBroker.PUT_SUBSCRIBER_OFFLINE, subscriber);

					double offEnd = offStart + distOFF.sample();
					if (offEnd <= SimulationConstants.SIMULATION_INTERVAL) {
						Log.printLine(CloudSim.clock() + ": " + getName() + ": Subscriber scheduled to go online at "
								+ offEnd);
						send(getId(), offEnd, PSDatacenterBroker.PUT_SUBSCRIBER_ONLINE, subscriber);
					}

					offStart = offEnd + distON.sample();
				}

				this.avgtON.put(networkPath.getPathId(), distON.getMean());
				this.avgtOFF.put(networkPath.getPathId(), distOFF.getMean());
			} else if (offRate > 0.0) {
				// If subscriber is only offline
				sendNow(getId(), PSDatacenterBroker.PUT_SUBSCRIBER_OFFLINE, subscriber);

				this.avgtON.put(networkPath.getPathId(), 0.0);
				this.avgtOFF.put(networkPath.getPathId(), offRate);
			} else {
				this.avgtON.put(networkPath.getPathId(), 0.0);
				this.avgtOFF.put(networkPath.getPathId(), 0.0);
			}
		}
	}

	/**
	 * Start cloudlets not yet initialized due subscriber disconnection
	 */
	private void startNewCloudlets(String subscriberId) {
		List<Vm> createdVms = getVmsCreatedList();
		PSNetworkPath networkPath = this.psNetwork.getNetworkPathWithSubscriber(subscriberId);

		for (Vm vm : createdVms) {
			PSCloudletSchedulerSpaceShared cloudletScheduler = (PSCloudletSchedulerSpaceShared) vm
					.getCloudletScheduler();
			Queue<PSCloudlet> newCloudlets = cloudletScheduler.getNewCloudlets();

			int n = newCloudlets.size();
			for (int i = 0; i < n; i++) {
				PSCloudlet cloudlet = newCloudlets.element();
				if (cloudlet.getPathId().equals(networkPath.getPathId())) {
					cloudletsSubmitted++;
					getCloudletSubmittedList().add(cloudlet);
					sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
					newCloudlets.remove();
				}
			}
		}
	}

	/**
	 * Resume previously paused cloudlets due subscriber disconnection
	 */
	private void resumePausedCloudlets(String subscriberId) {
		List<Vm> createdVms = getVmsCreatedList();
		PSNetworkPath networkPath = this.psNetwork.getNetworkPathWithSubscriber(subscriberId);

		for (Vm vm : createdVms) {
			PSCloudletSchedulerSpaceShared cloudletScheduler = (PSCloudletSchedulerSpaceShared) vm
					.getCloudletScheduler();
			Queue<PSCloudlet> pausedCloudlets = cloudletScheduler.getPausedCloudlets();

			int n = pausedCloudlets.size();
			for (int i = 0; i < n; i++) {
				PSCloudlet cloudlet = pausedCloudlets.element();
				if (cloudlet.getPathId().equals(networkPath.getPathId())) {
					sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_RESUME, cloudlet);
					pausedCloudlets.remove();
				}
			}
		}
	}

	/**
	 * Get next broker in path
	 * 
	 * @param pathId
	 * @param currentNodeId
	 * @return
	 */
	private DeployablePathNode getNextBrokerInPath(String pathId, String currentNodeId) {
		LinkedList<DeployablePathNode> pathBrokers = this.pathBrokersMap.get(pathId);

		boolean found = false;
		int i = 0;
		for (; i < pathBrokers.size(); i++) {
			if (pathBrokers.get(i).getTargetNode().getId().equals(currentNodeId)) {
				found = true;
				break;
			}
		}

		if (!found)
			throw new IllegalArgumentException(currentNodeId + " is not part of path " + pathId);

		if (i + 1 >= pathBrokers.size())
			return null;

		return pathBrokers.get(i + 1);
	}

	/**
	 * Print in the standard log the simulation results.<BR>
	 * It must be called. Otherwise {@link DatacenterBroker#finishExecution}
	 * must be explicitly called.
	 */
	/*
	 * public void calculateAndPrintResults() { // Calculate and print the
	 * response time Log.printLine("========== RESPONSE TIME ==========");
	 * Log.printLine("ID" + "\t" + "Submission" + "\t" + "RTin" + "\t" + "RTout"
	 * + "\t" + "RT");
	 * 
	 * Set<PSCloudlet> cloudlets = cloudletPubMap.keySet();
	 * 
	 * // Calculates the RT according with publish arrival time and subscriber
	 * // average finish time double avgRTInSum = 0.0; double avgRTOutSum = 0.0;
	 * double avgDprocSum = 0.0; double avgDtranSum = 0.0; int avgCount = 0; for
	 * (PSCloudlet cloudlet : cloudlets) { if (cloudlet.getType() ==
	 * cloudletType.IN) { double submissionTime = cloudlet.getSubmissionTime();
	 * double rtInSum = 0.0; double rtIn = 0.0; double rtOutSum = 0.0; double
	 * rtOut = 0.0; double DprocSum = 0.0; double Dproc = 0.0; double DtranSum =
	 * 0.0; double Dtran = 0.0; int subCount = 0;
	 * 
	 * // Calculate the average according with number of subscribers for
	 * (PSCloudlet subCloudlet : cloudletPubMap.get(cloudlet)) { double
	 * finishTime = subCloudlet.getFinishTime(); if (finishTime >= 0) { double
	 * rtInSub = cloudlet.getFinishTime() - cloudlet.getSubmissionTime(); double
	 * rtOutSub = subCloudlet.getFinishTime() - cloudlet.getSubmissionTime();
	 * DprocSum += cloudlet.getActualCPUTime(); DtranSum +=
	 * subCloudlet.getActualCPUTime(); rtInSum += rtInSub; rtOutSum += rtOutSub;
	 * subCount++; } } if (subCount > 0) { rtIn = rtInSum / subCount; rtOut =
	 * rtOutSum / subCount; Dproc = DprocSum / subCount; Dtran = DtranSum /
	 * subCount; Log.printLine(cloudlet.getCloudletId() + "\t" +
	 * String.format("%.4f", submissionTime) + "\t" + String.format("%.4f",
	 * rtIn) + "\t" + String.format("%.4f", rtOut) + "\t" +
	 * String.format("%.4f", (rtIn + rtOut)));
	 * 
	 * // This should'nt be necessary if (rtIn >= 0.0 && rtIn != Double.NaN) {
	 * avgRTInSum += rtIn; avgRTOutSum += rtOut; avgDprocSum += Dproc;
	 * avgDtranSum += Dtran; avgCount++; } } } }
	 * 
	 * avgRTIn = avgRTInSum / avgCount; avgRTOut = avgRTOutSum / avgCount;
	 * avgDproc = avgDprocSum / avgCount; avgDtran = avgDtranSum / avgCount;
	 * 
	 * Log.printLine("Average RT = " + (avgRTIn + avgRTOut)); Log.printLine(
	 * "tON = " + avgtON); Log.printLine("tOFF = " + avgtOFF);
	 * 
	 * super.finishExecution(); }
	 */

}
