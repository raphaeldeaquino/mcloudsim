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
		IN, OUT
	}

	private cloudletType type;
	private double arrivalTime;
	private String pathId;
	private String brokerId;

	public PSCloudlet(int cloudletId, String pathId, String brokerId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, cloudletType type, double arrivalTime) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		this.pathId = pathId;
		this.brokerId = brokerId;
		this.type = type;
		this.arrivalTime = arrivalTime;
	}

	public String getPathId() {
		return pathId;
	}

	public void setPathId(String pathId) {
		this.pathId = pathId;
	}
	
	public String getBrokerId() {
		return brokerId;
	}
	
	public void setBrokerId(String brokerId) {
		this.brokerId = brokerId;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public cloudletType getType() {
		return type;
	}
	
	public boolean isInput() {
		return this.type == cloudletType.IN;
	}
	
	public boolean isOutput() {
		return this.type == cloudletType.OUT;
	}
	
	public int compareTo(PSCloudlet o) {
		return this.getCloudletId() - o.getCloudletId();
	}

	@Override
	public String toString() {
		return type.toString().substring(0, 1) + "" + getCloudletId() + " [" + getExecStartTime() + ":"
				+ getFinishTime() + ":" + getActualCPUTime() + "]";
	}

}
