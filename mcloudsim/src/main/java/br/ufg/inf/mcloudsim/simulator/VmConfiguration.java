/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import org.cloudbus.cloudsim.CloudletScheduler;

/**
 * VMConfiguration represents the characteristics used to create a VM.
 * 
 * @author Raphael Gomes
 *
 */
public class VmConfiguration {

	private double mips;
	private int numberOfPes;
	private int ram;
	private long bw;
	private long size;
	private String vmm;
	private CloudletScheduler cloudletScheduler;
	private int reqPerSec;
	private double price;

	public VmConfiguration(double mips, int numberOfPes, int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		this.setMips(mips);
		this.setNumberOfPes(numberOfPes);
		this.setRam(ram);
		this.setBw(bw);
		this.setSize(size);
		this.setVmm(vmm);
		this.setCloudletScheduler(cloudletScheduler);
	}

	public double getMips() {
		return mips;
	}

	public void setMips(double mips) {
		this.mips = mips;
	}

	public int getNumberOfPes() {
		return numberOfPes;
	}

	public void setNumberOfPes(int numberOfPes) {
		this.numberOfPes = numberOfPes;
	}

	public int getRam() {
		return ram;
	}

	public void setRam(int ram) {
		this.ram = ram;
	}

	public long getBw() {
		return bw;
	}

	public void setBw(long bw) {
		this.bw = bw;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getVmm() {
		return vmm;
	}

	public void setVmm(String vmm) {
		this.vmm = vmm;
	}

	public CloudletScheduler getCloudletScheduler() {
		return cloudletScheduler;
	}

	public void setCloudletScheduler(CloudletScheduler cloudletScheduler) {
		this.cloudletScheduler = cloudletScheduler;
	}

	public int getReqPerSec() {
		return reqPerSec;
	}

	public void setReqPerSec(int reqPerSec) {
		this.reqPerSec = reqPerSec;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String toString() {
		return "[" + getNumberOfPes() + "][" + getMips() + "]";
	}

}
