/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import org.cloudbus.cloudsim.ResCloudlet;

/**
 * It's a extension of {@link ResCloudlet} to process only when subscriber is
 * online.
 * 
 * @author Raphael Gomes
 *
 */
public class PSResCloudlet extends ResCloudlet {

	/**
	 * @param cloudlet
	 *            The cloudlet being managed
	 */
	public PSResCloudlet(PSCloudlet cloudlet) {
		this(cloudlet, 0, 0, -1);
	}

	public PSResCloudlet(PSCloudlet cloudlet, long startTime, int duration, int reservID) {
		super(cloudlet, startTime, duration, reservID);
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
