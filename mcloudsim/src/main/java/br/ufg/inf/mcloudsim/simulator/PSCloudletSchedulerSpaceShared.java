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
// OK
public class PSCloudletSchedulerSpaceShared extends CloudletSchedulerSpaceShared {

	private Queue<PSCloudlet> pausedCloudlets;
	private Queue<PSCloudlet> newCloudlets;
	private PSNetworkPath psNetworkPath;
	private String nodeId;
	private boolean onOffBroker;
	private boolean networkInUse;
	private List<PSResCloudlet> cloudletTransmittedList;
	private List<PSResCloudlet> cloudletTransmissionList;
	private List<PSResCloudlet> cloudletWaitingTransmissionList;
	private List<PSResCloudlet> cloudletTransmissionPausedList;
	private double currentBwShare;

	/** The previous time. */
	private double previousTransmissionTime;

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
		this.networkInUse = false;
		this.pausedCloudlets = new LinkedList<>();
		this.newCloudlets = new LinkedList<>();
		this.psNetworkPath = psNetworkPath;
		this.nodeId = nodeId;
		this.onOffBroker = setOnOffBroker();
		this.cloudletTransmittedList = new ArrayList<>();
		this.cloudletTransmissionList = new ArrayList<>();
		this.cloudletWaitingTransmissionList = new ArrayList<>();
		this.cloudletTransmissionPausedList = new ArrayList<>();
		setPreviousTransmissionTime(0.0);
	}

	public double getPreviousTransmissionTime() {
		return previousTransmissionTime;
	}

	public void setPreviousTransmissionTime(double previousTransmissionTime) {
		this.previousTransmissionTime = previousTransmissionTime;
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
	// OK
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
			rcl.updateCloudletFinishedSoFar(cloudletFinishedSoFar);
			remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletLength() - cloudletFinishedSoFar);
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
	// OK
	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		PSCloudlet psCloudlet = (PSCloudlet) cloudlet;

		if (psCloudlet.isInput())
			Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Submiting input cloudlet "
					+ psCloudlet.getCloudletId() + " to broker " + nodeId);
		else
			throw new IllegalStateException("The method cloudletTransmit must be used in this case");

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

	// OK
	@Override
	public double cloudletResume(int cloudletId) {
		Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Resuming output cloudlet " + cloudletId
				+ " on broker " + nodeId);
		return super.cloudletResume(cloudletId);
	}

	// OK
	protected void setCurrentBwShare(Double bwShare) {
		this.currentBwShare = bwShare;
	}

	// OK
	public Double getCurrentBwShare() {
		return currentBwShare;
	}

	// OK
	public List<PSResCloudlet> getCloudletTransmissionList() {
		return cloudletTransmissionList;
	}

	// OK
	public List<PSResCloudlet> getCloudletWaitingTransmissionList() {
		return cloudletWaitingTransmissionList;
	}

	// OK
	public List<PSResCloudlet> getCloudletTransmissionPausedList() {
		return cloudletTransmissionPausedList;
	}

	// OK
	public double cloudletTransmit(Cloudlet cloudlet) {
		PSCloudlet psCloudlet = (PSCloudlet) cloudlet;

		if (psCloudlet.isOutput())
			Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Submiting output cloudlet "
					+ psCloudlet.getCloudletId() + " to broker " + nodeId);
		else
			throw new IllegalStateException("The method cloudletSubmit must be used in this case");

		// Check if this task is for an offline subscriber
		if (isOnOffBroker()) {
			Subscriber subscriber = this.psNetworkPath.getSubscriber();
			if (subscriber.isOffline()) {
				newCloudlets.add(psCloudlet);
				Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Cloudlet "
						+ psCloudlet.getCloudletId() + " not started due subscriber disconnection");
				return 0.0;
			}
		}

		// it can go to the trans list
		if (!networkInUse) {
			PSResCloudlet rcl = new PSResCloudlet((PSCloudlet) cloudlet);
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}
			getCloudletTransmissionList().add(rcl);
			networkInUse = true;
		} else {// no enough free PEs: go to the waiting queue
			PSResCloudlet rcl = new PSResCloudlet((PSCloudlet) cloudlet);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingTransmissionList().add(rcl);
			return 0.0;
		}

		// Mbit to Byte
		double capacity = getCurrentBwShare() * SimulationConstants.MBIT_TO_BYTE;

		// calculate the expected time for cloudlet completion
		return psCloudlet.getBytes() / capacity;
	}

	// OK
	public boolean isTransmittedCloudlets() {
		return getCloudletTransmittedList().size() > 0;
	}

	// OK
	public List<PSResCloudlet> getCloudletTransmittedList() {
		return cloudletTransmittedList;
	}

	// OK
	public Cloudlet getNextTransmittedCloudlet() {
		if (getCloudletTransmittedList().size() > 0) {
			return getCloudletTransmittedList().remove(0).getCloudlet();
		}
		return null;
	}

	// OK
	public boolean cloudletTransmissionPause(int cloudletId) {
		boolean found = false;
		int position = 0;

		// first, looks for the cloudlet in the trans list
		for (ResCloudlet rcl : getCloudletTransmissionList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// moves to the paused list
			PSResCloudlet rgl = getCloudletTransmissionList().remove(position);
			if (rgl.getRemainingCloudletBytes() == 0) {
				cloudletTransmissionFinish(rgl);
			} else {
				rgl.setCloudletStatus(Cloudlet.PAUSED);
				getCloudletTransmissionPausedList().add(rgl);
			}
			return true;

		}

		// now, look for the cloudlet in the waiting list
		position = 0;
		found = false;
		for (ResCloudlet rcl : getCloudletWaitingTransmissionList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// moves to the paused list
			PSResCloudlet rgl = getCloudletWaitingTransmissionList().remove(position);
			if (rgl.getRemainingCloudletBytes() == 0) {
				cloudletTransmissionFinish(rgl);
			} else {
				rgl.setCloudletStatus(Cloudlet.PAUSED);
				getCloudletTransmissionPausedList().add(rgl);
			}
			return true;

		}

		return false;
	}

	// OK
	public void cloudletTransmissionFinish(PSResCloudlet rcl) {
		rcl.setCloudletStatus(Cloudlet.SUCCESS);
		rcl.finalizeCloudletTransmission();
		getCloudletTransmittedList().add(rcl);
		networkInUse = false;
	}

	// OK
	public double cloudletTransmissionResume(int cloudletId) {
		boolean found = false;
		int position = 0;

		// look for the cloudlet in the paused list
		for (ResCloudlet rcl : getCloudletTransmissionPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			Log.printLine(CloudSim.clock() + ": " + getClass().getSimpleName() + ": Resuming output cloudlet "
					+ cloudletId + " on broker " + nodeId);

			PSResCloudlet rcl = getCloudletTransmissionPausedList().remove(position);

			// it can go to the trans list
			if (!networkInUse) {
				rcl.setCloudletStatus(Cloudlet.INEXEC);
				for (int i = 0; i < rcl.getNumberOfPes(); i++) {
					rcl.setMachineAndPeId(0, i);
				}

				getCloudletTransmissionList().add(rcl);
				networkInUse = true;

				// calculate the expected time for cloudlet completion
				double capacity = getCurrentBwShare() * SimulationConstants.MBIT_TO_BYTE;

				long remainingLength = rcl.getRemainingCloudletBytes();
				double estimatedFinishTime = CloudSim.clock() + (remainingLength / capacity);

				return estimatedFinishTime;
			} else {// no enough free PEs: go to the waiting queue
				rcl.setCloudletStatus(Cloudlet.QUEUED);

				long size = rcl.getRemainingCloudletBytes();
				((PSCloudlet) rcl.getCloudlet()).setBytes(size);

				getCloudletWaitingTransmissionList().add(rcl);
				return 0.0;
			}

		}

		// not found in the paused list: either it is in in the queue, executing
		// or not exist
		return 0.0;
	}

	// OK
	public double updateVmTransmission(double currentTime, Double bwShare) {
		setCurrentBwShare(bwShare);
		double timeSpam = currentTime - getPreviousTransmissionTime(); // time
																		// since
																		// last
		// update
		double capacity = bwShare * SimulationConstants.MBIT_TO_BYTE;
		Map<PSResCloudlet, Long> remainingCloudletLengthMap = new HashMap<>();
		Subscriber subscriber = psNetworkPath.getSubscriber();

		for (PSResCloudlet rcl : getCloudletTransmissionList()) {
			remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletBytes());
			long cloudletFinishedSoFar = (long) (capacity * timeSpam);
			if (isOnOffBroker() && ((PSCloudlet) rcl.getCloudlet()).isOutput()) {
				if (subscriber.isOnline()) {
					rcl.updateCloudletTransmittedSoFar(cloudletFinishedSoFar);
					remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletBytes() - cloudletFinishedSoFar);
				} else
					Log.printLine(CloudSim.clock() + ": Cloudlet " + rcl.getCloudletId()
							+ " paused due subscriber disconnection");
			} else {
				rcl.updateCloudletTransmittedSoFar(cloudletFinishedSoFar);
				remainingCloudletLengthMap.put(rcl, rcl.getRemainingCloudletBytes() - cloudletFinishedSoFar);
			}
		}

		// check the cloudlets of offline subscribers
		if (isOnOffBroker() && subscriber.isOffline() && timeSpam > 0.0) {
			for (PSResCloudlet rcl : remainingCloudletLengthMap.keySet()) {
				// If the cloudlet was not processed due subscriber
				// disconnection
				if (rcl.getRemainingCloudletBytes() == remainingCloudletLengthMap.get(rcl)) {
					boolean paused = cloudletTransmissionPause(rcl.getCloudletId());
					if (paused) {
						this.pausedCloudlets.add((PSCloudlet) rcl.getCloudlet());
						networkInUse = false;
					}
				}
			}
		}

		// no more cloudlets in this scheduler
		if (getCloudletTransmissionList().size() == 0 && getCloudletWaitingTransmissionList().size() == 0) {
			setPreviousTransmissionTime(currentTime);
			return 0.0;
		}

		// update each cloudlet
		int finished = 0;
		List<PSResCloudlet> toRemove = new ArrayList<>();
		for (PSResCloudlet rcl : getCloudletTransmissionList()) {
			// finished anyway, rounding issue...
			if (rcl.getRemainingCloudletBytes() <= 0) {
				toRemove.add(rcl);
				cloudletTransmissionFinish(rcl);
				finished++;
				remainingCloudletLengthMap.remove(rcl);
			}
		}
		getCloudletTransmissionList().removeAll(toRemove);

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingTransmissionList().isEmpty()) {
			for (int i = 0; i < finished; i++) {
				toRemove.clear();
				for (PSResCloudlet rcl : getCloudletWaitingTransmissionList()) {
					if (!networkInUse) {
						rcl.setCloudletStatus(Cloudlet.INEXEC);
						for (int k = 0; k < rcl.getNumberOfPes(); k++) {
							rcl.setMachineAndPeId(0, i);
						}
						getCloudletTransmissionList().add(rcl);
						networkInUse = true;
						toRemove.add(rcl);
						break;
					}
				}
				getCloudletWaitingTransmissionList().removeAll(toRemove);
			}
		}

		// estimate finish time of cloudlets in the execution queue
		double nextEvent = Double.MAX_VALUE;
		for (PSResCloudlet rcl : getCloudletTransmissionList()) {
			double remainingLength = rcl.getRemainingCloudletBytes();
			double estimatedFinishTime = currentTime + (remainingLength / capacity);
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}
		setPreviousTransmissionTime(currentTime);
		return nextEvent;
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