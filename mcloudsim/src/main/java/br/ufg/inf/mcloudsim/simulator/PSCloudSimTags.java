package br.ufg.inf.mcloudsim.simulator;

public abstract class PSCloudSimTags {

	/** Starting constant value for cloud-related tags. **/
	private static final int BASE = 0;

	/**
	 * Denotes the transmission start of a Cloudlet. This tag is normally used
	 * between CloudSim User and CloudResource entity.
	 */
	public static final int CLOUDLET_TRANSMISSION_START = BASE + 49;

	/** Pauses a Cloudlet submitted in the CloudResource entity. */
	public static final int CLOUDLET_TRANSMISSION_PAUSE = BASE + 50;

	/** Resumes a Cloudlet submitted in the CloudResource entity. */
	public static final int CLOUDLET_TRANSMISSION_RESUME = BASE + 51;
	
	public static final int CLOUDLET_TRANSMITTED = BASE + 52;
	
	public static final int VM_DATACENTER_EVENT_TRANSMISSION = BASE + 53;}
