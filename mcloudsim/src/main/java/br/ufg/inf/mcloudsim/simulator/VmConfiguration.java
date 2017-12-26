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
	private double ram;
	private double bw;
	private double size;
	private String vmm;
	private Class<? extends CloudletScheduler> cloudletSchedulerClass;
	private int reqPerSec;
	private String name;
	private String provider;
	private String region;
	private double price;

	public VmConfiguration(double mips, int numberOfPes, double ram, long bw, double size, String vmm,
			Class<? extends CloudletScheduler> cloudletSchedulerClass) {
		this.setMips(mips);
		this.setNumberOfPes(numberOfPes);
		this.setRam(ram);
		this.setBw(bw);
		this.setSize(size);
		this.setVmm(vmm);
		this.setCloudletSchedulerClass(cloudletSchedulerClass);
	}

	public VmConfiguration(double mips, int numberOfPes, double ram, double bw, double size, String name, String provider,
			String region, double price) {
		this.setMips(mips);
		this.setNumberOfPes(numberOfPes);
		this.setRam(ram);
		this.setBw(bw);
		this.setSize(size);
		this.setName(name);
		this.setProvider(provider);
		this.setRegion(region);
		this.setPrice(price);
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

	public double getRam() {
		return ram;
	}

	public void setRam(double ram) {
		this.ram = ram;
	}

	public double getBw() {
		return bw;
	}

	public void setBw(double bw) {
		this.bw = bw;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getVmm() {
		return vmm;
	}

	public void setVmm(String vmm) {
		this.vmm = vmm;
	}

	public Class<? extends CloudletScheduler> getCloudletSchedulerClass() {
		return cloudletSchedulerClass;
	}

	public void setCloudletSchedulerClass(Class<? extends CloudletScheduler> cloudletSchedulerClass) {
		this.cloudletSchedulerClass = cloudletSchedulerClass;
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
	
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String toString() {
		return "[" + getNumberOfPes() + "][" + getMips() + "]";
	}

}
