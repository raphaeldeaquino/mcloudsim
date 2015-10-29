/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import br.ufg.inf.mcloudsim.experiment.ExperimentConstants;
import br.ufg.inf.mcloudsim.simulator.VmConfiguration;

/**
 * Functions to help simulation developing
 * 
 * @author Raphael Gomes
 *
 */
public class Helper {
	private static int hashSuffix = 1000;

	/**
	 * Generates a hash number according with a given string
	 * 
	 * @param s
	 *            the string representing the key
	 * @return the hash value of string
	 */
	public static int hash(String s) {
		int h = 0;

		// The hash value is the number the string represents or a function
		// using ASCII value
		try {
			h = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			for (int i = 0; i < s.length(); i++) {
				h += s.charAt(i) - 48; // Don't use the ASCII value but the real
										// value
			}
		}
		// To have different values we concatenate the hash value if current
		// time
		String hr = String.valueOf(h) + String.valueOf(hashSuffix++);
		return Integer.parseInt(hr);
	}

	/**
	 * Creates a datacenter
	 * 
	 * @param name
	 *            the datacenter name
	 * @return the datacenter
	 */
	public static Datacenter createDatacenter(String name) {
		// Here are the steps needed to create a Datacenter:
		// We need to create a list to store our machine
		List<Host> hostList = createHostList(ExperimentConstants.HOST_NUMBER);

		// Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located

		// the cost of using processing in this resource
		double cost = ExperimentConstants.BROKER_VM_COST_PER_PROCESSING;

		// the cost of using memory in this resource
		double costPerMem = ExperimentConstants.BROKER_VM_COST_PER_MEMORY;

		// the cost of using storage in this resource
		double costPerStorage = ExperimentConstants.BROKER_VM_COST_PER_STORAGE;

		// the cost of using bw in this resource
		double costPerBw = 0.0;

		// we are not adding SAN devices by now
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// We need to create a Datacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	/**
	 * Creates a list of VMs
	 * 
	 * @param configuration
	 *            the configuration used to create VMs
	 * @param vmsNumber
	 *            the quantity of created VMs
	 * @param userId
	 *            the VM owner
	 * @return the list of VMs
	 */
	public static List<Vm> createVmList(VmConfiguration configuration, int vmsNumber, int userId) {
		List<Vm> vms = new LinkedList<Vm>();
		for (int i = 0; i < vmsNumber; i++) {
			vms.add(new Vm(i, userId, configuration.getMips(), configuration.getNumberOfPes(), configuration.getRam(),
					configuration.getBw(), configuration.getSize(), configuration.getVmm(),
					configuration.getCloudletScheduler()));
		}
		return vms;
	}

	public static DatacenterBroker createBroker(Class<? extends DatacenterBroker> classType) {
		DatacenterBroker broker = null;
		try {
			broker = classType.getConstructor(String.class).newInstance(classType.getSimpleName());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return broker;
	}

	/**
	 * Creates a list of hosts
	 * 
	 * @param hostsNumber
	 *            the quantity of hosts
	 * @return the list of hosts
	 */
	public static List<Host> createHostList(int hostsNumber) {
		List<Host> hostList = new LinkedList<Host>();
		for (int i = 0; i < hostsNumber; i++) {
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < ExperimentConstants.HOST_NUMBER_OF_PES; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(ExperimentConstants.HOST_MIPS)));
			}

			try {
				hostList.add(new Host(i, new RamProvisionerSimple(ExperimentConstants.HOST_RAM),
						new BwProvisionerSimple(ExperimentConstants.HOST_BW), ExperimentConstants.HOST_STORAGE, peList,
						ExperimentConstants.BROKER_VM_SCHEDULER.getConstructor(List.class).newInstance(peList)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return hostList;
	}

	/**
	 * Prints a list of cloudlets in a formatted way in the configured log
	 * 
	 * @param list
	 *            the list of cloudlets
	 */
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		DecimalFormat fmt = new DecimalFormat("0.0000");

		Log.printLine("Cloudlets: " + list.size());
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + "\t" + "STATUS" + "\t" + "Lenght" + "\t" + "Data center ID" + "\t" + "VM ID"
				+ "\t" + "Time" + "\t" + "Arrival" + "\t" + "Start" + "\t" + "Finish");

		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(cloudlet.getCloudletId() + "\t");

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine("\t" + cloudlet.getCloudletLength() + "\t" + cloudlet.getResourceId() + "\t#"
						+ cloudlet.getVmId() + "\t" + fmt.format(cloudlet.getActualCPUTime()).replace('.', ',') + "\t"
						+ fmt.format(cloudlet.getSubmissionTime()).replace('.', ',') + "\t"
						+ fmt.format(cloudlet.getExecStartTime()).replace('.', ',') + "\t"
						+ fmt.format(cloudlet.getFinishTime()).replace('.', ','));
			}
		}
	}
}
