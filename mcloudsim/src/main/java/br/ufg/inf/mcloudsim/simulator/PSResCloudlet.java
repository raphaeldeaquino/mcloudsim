/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

import br.ufg.inf.mcloudsim.core.ConnectivityStatus;
import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.Subscriber;

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
		super(cloudlet);
	}

	public PSResCloudlet(PSCloudlet cloudlet, long startTime, int duration, int reservID) {
		super(cloudlet, startTime, duration, reservID);
	}

	/**
	 * Updates processing only if subscriber is online
	 */
	@Override
	public void updateCloudletFinishedSoFar(long miLength) {
		EventEntity entity = PSDatacenterBroker.cloudletMap.get(this.getCloudlet());

		if (entity instanceof Subscriber && ((Subscriber) entity).getStatus() == ConnectivityStatus.OFFLINE) {
			Log.printLine(CloudSim.clock() + ": Cloudlet " + getCloudletId()
					+ " didn't processed due subscriber disconnection");
		} else {
			super.updateCloudletFinishedSoFar(miLength);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getCloudletId() + ":" + getCloudletArrivalTime();
	}

}
