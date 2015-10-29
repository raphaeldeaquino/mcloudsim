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
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * A cloudlet type for representing pub/sub interaction. <BR>
 * An PSCloudlet can be of type PUBLISH or SUBSCRIBE. It inherits
 * {@link Cloudlet} with this information and the arrival time of cloudlet. <BR>
 * The information used to compare two cloudlets is the identifier.
 * 
 * @author Raphael Gomes
 *
 */
public class PSCloudlet extends Cloudlet implements Comparable<PSCloudlet> {

	public enum cloudletType {
		PUBLISH, SUBSCRIBE
	}

	private cloudletType type;
	private double arrivalTime;

	public PSCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, cloudletType type, double arrivalTime) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		this.type = type;
		this.arrivalTime = arrivalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public cloudletType getType() {
		return type;
	}

	public int compareTo(PSCloudlet o) {
		return this.getCloudletId() - o.getCloudletId();
	}

}
