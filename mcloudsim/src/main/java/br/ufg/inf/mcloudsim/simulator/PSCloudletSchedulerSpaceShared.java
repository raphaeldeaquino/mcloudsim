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
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;

/**
 * Cloudlet scheduler with space shared scheduling. <br>
 * It performs the same functionalities of {@link CloudletSchedulerSpaceShared}
 * but not processing and rescheduling cloudlets regarding offline subscribers.
 * 
 * @author Raphael Gomes
 *
 */
public class PSCloudletSchedulerSpaceShared extends CloudletSchedulerSpaceShared {

	/** The current CPUs. */
	protected int currentCpus;

	/** The used PEs. */
	protected int usedPes;

	/**
	 * Creates a new CloudletSchedulerSpaceShared object. This method must be
	 * invoked before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public PSCloudletSchedulerSpaceShared() {
		super();
		usedPes = 0;
		currentCpus = 0;
	}

	/**
	 * Do the same of
	 * {@link CloudletSchedulerSpaceShared#updateVmProcessing(double, List)} but
	 * rescheduling cloudlets which not processed due subscriber disconnection.d
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		double timeSpam = currentTime - getPreviousTime(); // time since last
															// update
		double capacity = 0.0;
		int cpus = 0;
		Map<ResCloudlet, Long> remainingCloudletLength = new HashMap<ResCloudlet, Long>();

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
			remainingCloudletLength.put(rcl, rcl.getRemainingCloudletLength());
			rcl.updateCloudletFinishedSoFar((long) (capacity * timeSpam * rcl.getNumberOfPes() * Consts.MILLION));
			Log.printLine("PSCloudletSchedulerSpaceShared " + rcl.getCloudletId() + " (updateVmProcessing)=" + capacity
					+ " " + timeSpam);
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
			if (rcl.getRemainingCloudletLength() == 0) {
				toRemove.add(rcl);
				cloudletFinish(rcl);
				finished++;
				remainingCloudletLength.remove(rcl);
			}
		}
		getCloudletExecList().removeAll(toRemove);

		// check the cloudlets of offline subscribers
		toRemove.clear();
		for (ResCloudlet rcl : remainingCloudletLength.keySet()) {
			// If the cloudlet was not processed due subscriber disconnection
			if (rcl.getRemainingCloudletLength() == remainingCloudletLength.get(rcl)) {
				toRemove.add(rcl);
				rcl.setCloudletStatus(Cloudlet.QUEUED);
			}
		}
		getCloudletExecList().removeAll(toRemove);
		for (int i = 0; i < toRemove.size(); i++) {
			if (!getCloudletWaitingList().isEmpty()) {
				ResCloudlet rcl = getCloudletWaitingList().get(0);
				rcl.setCloudletStatus(Cloudlet.INEXEC);
				for (int k = 0; k < rcl.getNumberOfPes(); k++) {
					rcl.setMachineAndPeId(0, i);
				}
				getCloudletExecList().add(rcl);
				getCloudletWaitingList().remove(0);
			} else {
				ResCloudlet rcl = toRemove.get(0);
				rcl.setCloudletStatus(Cloudlet.INEXEC);
				for (int k = 0; k < rcl.getNumberOfPes(); k++) {
					rcl.setMachineAndPeId(0, i);
				}
				getCloudletExecList().add(rcl);
				toRemove.remove(0);
			}
		}
		getCloudletWaitingList().addAll(toRemove);

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
				Log.printLine("PSCloudletSpace=" + currentTime + " " + remainingLength + " " + capacity);
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

}
