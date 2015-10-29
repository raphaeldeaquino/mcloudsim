/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.experiment;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;

/**
 * Constants used in the experiment
 * 
 * @author Raphael Gomes
 *
 */
public interface ExperimentConstants {
	/** How many hosts will have the private cloud */
	public final static int HOST_NUMBER = 100;

	/** How many users executes the simulation */
	public final static int NUM_USER = 1;

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
	public final static Class<? extends UtilizationModel> CLOUDLET_UTILIZATION_MODEL = UtilizationModelFull.class;
	public final static Class<? extends CloudletScheduler> CLOUDLET_SCHEDULER = CloudletSchedulerSpaceShared.class;

	/** How many times the simulatin is performed */
	public static final int EXPERIMENT_RUNS = 10;

	/** How long each simulation runs **/
	public static final int SIMULATION_INTERVAL = (int) (10 * 60 * 60);

	/** VM configuration to run pub/sub broker */
	public static final int BROKER_VM_MIPS = 800;
	public static final int BROKER_VM_NUMBER_OF_PES = 1;
	public static final int BROKER_VM_RAM = 768;
	public static final int BROKER_VM_BW = 1000;
	public static final int BROKER_VM_SIZE = 10000;
	public final static String BROKER_VM_VMM = "Xen";
	public static final double BROKER_VM_COST_PER_PROCESSING = 0.09;
	public static final double BROKER_VM_COST_PER_MEMORY = 0.05;
	public static final double BROKER_VM_COST_PER_STORAGE = 0.01;
	public final static Class<? extends VmScheduler> BROKER_VM_SCHEDULER = VmSchedulerSpaceShared.class;

	/** How long is file transfer in network */
	public static final double LATENCY_OVERHEAD = 0;

	/** Publish rate (events/time unit) */
	public static final double[] PUBLISH_RATE = { 1.5 };// , 0.1, 0.5, 1, 1.5,
														// 2, 2.5, 3, 3.5, 3.7,
														// 3.9, 3.95, 4};
	
	/** Single topic used in experiments */
	public static final String TOPIC = "TOPIC.1.0";

	/** How many subscribers to each topic */
	public static final int SUBSCRIBE_NUMBER = 1;	

	/** I/O size */
	public static final long PUBLISH_CLOUDLET_FILESIZE = 0;
	public static final long SUBSCRIBE_CLOUDLET_FILESIZE = PUBLISH_CLOUDLET_FILESIZE;

	/**
	 * Connectivity distribution rate. The time of connection/disconnection is
	 * given by 1/SUBSCRIBE_ON_OFF_RATE
	 */
	public static final double SUBSCRIBE_ON_OFF_RATE = 0.05;

	/**
	 * How much (in % of module) the measured connectivity distribution must be
	 * from expected
	 */
	public static final double SUBSCRIBE_ON_OFF_DIFF = 0.8;

	/** How long time units the publish event takes to be processed */
	public static final double SERVICE_TIME_IN = 0.005;

	/** How long time units the subscribe event takes to be processed */
	public static final double SERVICE_TIME_OUT = 0.125;
}
