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

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.network.DeployablePathNode;
import br.ufg.inf.mcloudsim.network.PSNetworkPath;

/**
 * Cloudlet scheduler with space shared scheduling. <br>
 * It performs the same functionalities of {@link CloudletSchedulerSpaceShared}
 * but not processing and rescheduling cloudlets regarding offline subscribers.
 * 
 * @author Raphael Gomes
 *
 */
public class PSCloudletSchedulerSpaceShared extends CloudletSchedulerSpaceShared {

	private Queue<PSCloudlet> pausedCloudlets;
	private Queue<PSCloudlet> newCloudlets;
	private PSNetworkPath psNetworkPath;
	private String nodeId;
	private boolean onOffBroker;

	/**
	 * Creates a new CloudletSchedulerSpaceShared object. This method must be
	 * invoked before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public PSCloudletSchedulerSpaceShared(PSNetworkPath psNetworkPath, String nodeId) {
		super();
		usedPes = 0;
		currentCpus = 0;
		this.pausedCloudlets = new LinkedList<>();
		this.newCloudlets = new LinkedList<>();
		this.psNetworkPath = psNetworkPath;
		this.nodeId = nodeId;
		this.onOffBroker = setOnOffBroker();
	}

	public Queue<PSCloudlet> getPausedCloudlets() {
		return pausedCloudlets;
	}

	public Queue<PSCloudlet> getNewCloudlets() {
		return newCloudlets;
	}

	public boolean isOnOffBroker() {
		return onOffBroker;
	}

	/**
	 * Do the same of
	 * {@link CloudletSchedulerSpaceShared#updateVmProcessing(double, List)} but
	 * rescheduling cloudlets that were not processed due subscriber
	 * disconnection
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		double timeSpam = currentTime - getPreviousTime(); // time since last
															// update
		double capacity = 0.0;
		int cpus = 0;
		Map<ResCloudlet, Long> remainingCloudletLengthMap = new HashMap<>();

		for (Double mips : mipsShare) { // count the CPUs available to the VMM
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu

		// each machine in the exec list has the same amount of cpu
		for (ResCloudlet rcl : getCloudletExecList()) {
			remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletLength());
			long cloudletFinishedSoFar = (long) (capacity * timeSpam * rcl.getNumberOfPes() * Consts.MILLION);
			if (isOnOffBroker() && ((PSCloudlet) rcl.getCloudlet()).isOutput()) {
				Subscriber subscriber = psNetworkPath.getSubscriber();
				if (subscriber.isOnline()) {
					rcl.updateCloudletFinishedSoFar(cloudletFinishedSoFar);
					remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletLength() - cloudletFinishedSoFar);
				} else
					Log.printLine(CloudSim.clock() + ": Cloudlet " + rcl.getCloudletId()
							+ " paused due subscriber disconnection");
			} else {
				rcl.updateCloudletFinishedSoFar(cloudletFinishedSoFar);
				remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletLength() - cloudletFinishedSoFar);
			}
		}

		// check the cloudlets of offline subscribers
		for (ResCloudlet rcl : remainingCloudletLengthMap.keySet()) {
			// If the cloudlet was not processed due subscriber disconnection
			if (rcl.getRemainingCloudletLength() == remainingCloudletLengthMap.get(rcl)) {
				boolean paused = cloudletPause(rcl.getCloudletId());
				if (paused) {
					this.pausedCloudlets.add((PSCloudlet) rcl.getCloudlet());
					usedPes -= rcl.getNumberOfPes();
				}
			}
		}

		// no more cloudlets in this scheduler
		if (getCloudletExecList().size() == 0 && getCloudletWaitingList().size() == 0) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// update each cloudlet
		int finished = 0;
		List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			// finished anyway, rounding issue...
			if (rcl.getRemainingCloudletLength() <= 0) {
				toRemove.add(rcl);
				cloudletFinish(rcl);
				finished++;
				remainingCloudletLengthMap.remove(rcl);
			}
		}
		getCloudletExecList().removeAll(toRemove);

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingList().isEmpty()) {
			for (int i = 0; i < finished; i++) {
				toRemove.clear();
				for (ResCloudlet rcl : getCloudletWaitingList()) {
					if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
						rcl.setCloudletStatus(Cloudlet.INEXEC);
						for (int k = 0; k < rcl.getNumberOfPes(); k++) {
							rcl.setMachineAndPeId(0, i);
						}
						getCloudletExecList().add(rcl);
						usedPes += rcl.getNumberOfPes();
						toRemove.add(rcl);
						break;
					}
				}
				getCloudletWaitingList().removeAll(toRemove);
			}
		}

		// estimate finish time of cloudlets in the execution queue
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double remainingLength = rcl.getRemainingCloudletLength();
			double estimatedFinishTime = currentTime + (remainingLength / (capacity * rcl.getNumberOfPes()));
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}
		setPreviousTime(currentTime);
		return nextEvent;
	}

	/**
	 * Modification of
	 * {@link CloudletSchedulerSpaceShared#cloudletSubmit(Cloudlet, double)} to
	 * create instances of PSResCloudlet instead of {@link ResCloudlet}.
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		PSCloudlet psCloudlet = (PSCloudlet) cloudlet;

		if (psCloudlet.isInput())
			Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Submiting input cloudlet "
					+ psCloudlet.getCloudletId() + " to broker " + nodeId);
		else
			Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Submiting output cloudlet "
					+ psCloudlet.getCloudletId() + " to broker " + nodeId);

		// Check if this task is for an offline subscriber
		if (psCloudlet.isOutput() && isOnOffBroker()) {
			Subscriber subscriber = this.psNetworkPath.getSubscriber();
			if (subscriber.isOffline()) {
				newCloudlets.add(psCloudlet);
				Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Cloudlet "
						+ psCloudlet.getCloudletId() + " not started due subscriber disconnection");
				return 0.0;
			}
		}

		// it can go to the exec list
		if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes()) {
			ResCloudlet rcl = new PSResCloudlet((PSCloudlet) cloudlet);
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}
			getCloudletExecList().add(rcl);
			usedPes += cloudlet.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue
			ResCloudlet rcl = new PSResCloudlet((PSCloudlet) cloudlet);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingList().add(rcl);
			return 0.0;
		}

		// calculate the expected time for cloudlet completion
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : getCurrentMipsShare()) {
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}

		currentCpus = cpus;
		capacity /= cpus;

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = cloudlet.getCloudletLength();
		length += extraSize;
		cloudlet.setCloudletLength(length);
		return cloudlet.getCloudletLength() / capacity;
	}
	
	@Override
	public double cloudletResume(int cloudletId) {
		Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Resuming output cloudlet "
				+ cloudletId + " on broker " + nodeId);
		return super.cloudletResume(cloudletId);
	}

	/**
	 * Check if this cloudlet scheduler manages an On OFF broker
	 * 
	 * @return
	 */
	private boolean setOnOffBroker() {
		LinkedList<DeployablePathNode> brokersPath = this.psNetworkPath.getBrokersPath();

		for (int i = 0; i < brokersPath.size(); i++) {
			if (brokersPath.get(i).getTargetNode().getId().equals(this.nodeId)) {
				if (i == brokersPath.size() - 1)
					return true;
				else
					return false;
			}
		}

		throw new RuntimeException("Broker " + this.nodeId + " not found in path " + this.psNetworkPath);
	}

	@Override
	public String toString() {
		return "CS for " + nodeId;
	}
}
