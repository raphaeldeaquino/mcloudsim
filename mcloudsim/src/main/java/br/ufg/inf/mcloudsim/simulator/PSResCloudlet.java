/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.ResCloudlet;

/**
 * It's a extension of {@link ResCloudlet} to process only when subscriber is
 * online.
 * 
 * @author Raphael Gomes
 *
 */
// OK
public class PSResCloudlet extends ResCloudlet {

	private long cloudletTransmittedSoFar;

	/**
	 * @param cloudlet
	 *            The cloudlet being managed
	 */
	public PSResCloudlet(PSCloudlet cloudlet) {
		this(cloudlet, 0, 0, -1);
	}

	public PSResCloudlet(PSCloudlet cloudlet, long startTime, int duration, int reservID) {
		super(cloudlet, startTime, duration, reservID);
		this.init();
	}

	// OK
	private void init() {
		// In case a Cloudlet has been executed partially by some other grid
		// hostList.
		cloudletTransmittedSoFar = ((PSCloudlet) getCloudlet()).getCloudletTransmittedSoFar();
	}

	// OK
	public long getRemainingCloudletBytes() {
		long length = (long) (((PSCloudlet) getCloudlet()).getBytes() - cloudletTransmittedSoFar);

		// Remaining Cloudlet length can't be negative number.
		if (length < 0) {
			return 0;
		}

		return length;
	}

	// OK
	public void finalizeCloudletTransmission() {
		long finished = 0;
		// if (cloudlet.getCloudletTotalLength() * Consts.MILLION <
		// cloudletFinishedSoFar) {
		if (getCloudlet().getCloudletStatus() == Cloudlet.SUCCESS) {
			finished = ((PSCloudlet)getCloudlet()).getBytes();
		} else {
			finished = cloudletTransmittedSoFar;
		}

		((PSCloudlet)getCloudlet()).setCloudletTransmittedSoFar(finished);
	}

	public void updateCloudletTransmittedSoFar(long bytes) {
		cloudletTransmittedSoFar += bytes;
		
		if (((PSCloudlet)getCloudlet()).getBytes() - cloudletTransmittedSoFar == 1) {
			cloudletTransmittedSoFar++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getCloudletId() + "[" + getCloudlet().getExecStartTime() + ":" + getCloudlet().getFinishTime() + "]";
	}

}
