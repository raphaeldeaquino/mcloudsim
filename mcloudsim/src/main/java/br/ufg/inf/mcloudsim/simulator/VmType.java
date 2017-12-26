package br.ufg.inf.mcloudsim.simulator;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

public class VmType extends Vm {

	public static final String DEFAULT_VMM = "Xen";

	private String name;
	private double ram;
	private double bw;
	private double size;
	private String provider;
	private String region;
	private double price;

	public VmType(String name, int id, int userId, double mips, int numberOfPes, double ram, double bw, double size,
			CloudletScheduler cloudletScheduler, String provider, String region, double price) {
		super(id, userId, mips, numberOfPes, (int) ram, (long) bw, (long) size, DEFAULT_VMM, cloudletScheduler);
		this.name = name;
		this.ram = ram;
		this.bw = bw;
		this.size = size;
		this.provider = provider;
		this.region = region;
		this.price = price;
	}
	
	

	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public double getTotalRam() {
		return ram;
	}

	@Override
	public int getRam() {
		return (int) ram;
	}

	public void setRam(double ram) {
		this.ram = ram;
	}

	@Override
	public long getBw() {
		return (long) bw;
	}

	public void setBw(double bw) {
		this.bw = bw;
	}

	public double getTotalSize() {
		return size;
	}

	@Override
	public long getSize() {
		return (long) size;
	}

	public void setSize(double size) {
		this.size = size;
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	@Override
	public String toString() {
		return name + " #" + getId();
	}

}
