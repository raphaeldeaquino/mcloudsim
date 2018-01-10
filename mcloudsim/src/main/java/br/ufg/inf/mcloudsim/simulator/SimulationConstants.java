/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import org.cloudbus.cloudsim.VmScheduler;

/**
 * Constants used in the experiment
 * 
 * @author Raphael Gomes
 *
 */
public interface SimulationConstants {
	
	/** How many times the simulation is performed */
	public static final int EXPERIMENT_RUNS = 1;
	
	/** How long each simulation runs **/
	public static final int SIMULATION_INTERVAL = (int) (2 * 60.0 * 60.0);
	
	/** Publish rate (events/time unit) */
	//public static final double[] PUBLISH_RATE = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 3.7, 3.9};
	//public static final double[] PUBLISH_RATE = {0.5, 1.0, 5.0, 10.0, 15.0, 20.0, 21.0, 22.0, 23.0};
	public static final double[] PUBLISH_RATE = {2.5};
	
	/** File with pub/sub network description */
	public static final String PS_NETWORK_DESCRIPTOR = "simulation.xml";
	
	public static final double MINIMUM_TIME_BETWEEN_EVENTS = 0.01;
	
	/** How many hosts will have the private cloud */
	public final static int NUM_HOST = 100;

	/**
	 * Host configuration. We used here the configuration of Intel® Xeon®
	 * Processor X5570, 8 Gb of ram
	 */
	public final static int HOST_MIPS = 24000; // MIPS per core
	public final static int HOST_NUMBER_OF_PES = 16;
	public final static int HOST_RAM = 65536;
	public final static int HOST_BW = 1000000;
	public final static long HOST_STORAGE = 500000000;

	/**
	 * Cloudlet configuration. In our simulation there is no I/O and the
	 * scheduler is space shared
	 */
	public final static int CLOUDLET_PES = 1;
	public final static long CLOUDLET_FILESIZE = 0;
	public final static long CLOUDLET_OUTPUT_SIZE = 0;

	/** VM configuration to run pub/sub broker */
	public static final int BROKER_VM_MIPS = 800;
	public static final int BROKER_VM_NUMBER_OF_PES = 4;
	public static final int BROKER_VM_RAM = 768;
	public static final int BROKER_VM_BW = 1000;
	public static final int BROKER_VM_SIZE = 10000;
	public final static String BROKER_VM_VMM = "Xen";
	public static final double BROKER_VM_COST_PER_PROCESSING = 0.09;
	public static final double BROKER_VM_COST_PER_MEMORY = 0.05;
	public static final double BROKER_VM_COST_PER_STORAGE = 0.01;
	public final static Class<? extends VmScheduler> BROKER_VM_SCHEDULER = PSVmSchedulerSpaceShared.class;

	/** How long is file transfer in network */
	public static final double LATENCY_OVERHEAD = 0;

	/** I/O size */
	public static final long PUBLISH_CLOUDLET_FILESIZE = 0;
	public static final long SUBSCRIBE_CLOUDLET_FILESIZE = PUBLISH_CLOUDLET_FILESIZE;
	
	public static final double MBIT_TO_BYTE = 125000;
}