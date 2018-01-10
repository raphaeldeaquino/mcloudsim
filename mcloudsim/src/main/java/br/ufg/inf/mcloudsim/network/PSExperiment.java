/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.network;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.PSNetworkRegistry;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.simulator.PSCloudlet;
import br.ufg.inf.mcloudsim.simulator.PSCloudletSchedulerSpaceShared;
import br.ufg.inf.mcloudsim.simulator.PSDatacenterBroker;
import br.ufg.inf.mcloudsim.simulator.SimulationConstants;
import br.ufg.inf.mcloudsim.simulator.SubscriberRegistry;
import br.ufg.inf.mcloudsim.simulator.VmConfiguration;
import br.ufg.inf.mcloudsim.utils.Helper;

/**
 * Simulates a pub/sub broker with subscribers connectivity distribution
 * 
 * @author Raphael Gomes
 *
 */
public class PSExperiment {

	private static Logger logger = Logger.getLogger(PSExperiment.class.getName());

	public static int cloudletCount;

	protected Datacenter datacenter;

	protected PSDatacenterBroker broker;

	protected List<Vm> vmlist;

	protected List<Cloudlet> cloudletList = new LinkedList<Cloudlet>();

	protected Map<String, List<Double>> resultsMap = new HashMap<String, List<Double>>();

	public PSExperiment() {
		cloudletCount = 0;
		EventEntity.reset();
		PSNetworkRegistry.reset();
	}

	protected int getBrokerId() {
		return broker.getId();
	}

	/*
	 * protected int getDatacenterId() { return datacenter.getId(); }
	 */

	protected List<Cloudlet> createCloudletList(Publisher publisher) {
		PoissonDistribution distLoad = new PoissonDistribution(publisher.getRate());
		Random random = new Random(System.currentTimeMillis());
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

		// For each second of simulation submit 'rate' requests
		double accLoad = 0.0;
		for (int time = 1; time <= SimulationConstants.SIMULATION_INTERVAL; time++) {
			double nEvents = distLoad.sample();

			// How many events for this iteration
			long nGenEvents = (long) nEvents;

			// How many not complete events
			accLoad += nEvents - nGenEvents;

			// Events for this iteration
			while (nGenEvents >= 1) {
				PSCloudlet cloudlet = new PSCloudlet(++cloudletCount, publisher.getId(),
						(long) (SimulationConstants.SERVICE_TIME_PROCESSING * SimulationConstants.BROKER_VM_MIPS), 1,
						SimulationConstants.PUBLISH_CLOUDLET_FILESIZE, SimulationConstants.CLOUDLET_OUTPUT_SIZE,
						new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(),
						PSCloudlet.cloudletType.IN, Math.min(time, (time - 1) + random.nextDouble()));
				cloudletList.add(cloudlet);
				PSDatacenterBroker.getCloudletMap().put(cloudlet, publisher);
				PSDatacenterBroker.getCloudletPubMap().put(cloudlet, new ArrayList<PSCloudlet>());
				nGenEvents--;
			}

			// Accumulated events from previous iterations
			while (accLoad >= 1.0) {
				PSCloudlet cloudlet = new PSCloudlet(++cloudletCount, publisher.getId(),
						(long) (SimulationConstants.SERVICE_TIME_PROCESSING * SimulationConstants.BROKER_VM_MIPS), 1,
						SimulationConstants.PUBLISH_CLOUDLET_FILESIZE, SimulationConstants.CLOUDLET_OUTPUT_SIZE,
						new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(),
						PSCloudlet.cloudletType.IN, Math.min(time, (time - 1) + random.nextDouble()));
				cloudletList.add(cloudlet);
				PSDatacenterBroker.getCloudletMap().put(cloudlet, publisher);
				PSDatacenterBroker.getCloudletPubMap().put(cloudlet, new ArrayList<PSCloudlet>());
				accLoad--;
			}
		}

		while (cloudletList.size() > (double) SimulationConstants.SIMULATION_INTERVAL * publisher.getRate()) {
			int iToRemove = random.nextInt(cloudletList.size());
			cloudletList.remove(iToRemove);
		}

		while (cloudletList.size() < (double) SimulationConstants.SIMULATION_INTERVAL * publisher.getRate()) {
			int iToAdd = random.nextInt(cloudletList.size());
			PSCloudlet cloudlet = new PSCloudlet(++cloudletCount, publisher.getId(),
					(long) (SimulationConstants.SERVICE_TIME_PROCESSING * SimulationConstants.BROKER_VM_MIPS), 1,
					SimulationConstants.PUBLISH_CLOUDLET_FILESIZE, SimulationConstants.CLOUDLET_OUTPUT_SIZE,
					new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(),
					PSCloudlet.cloudletType.IN, Math.min(iToAdd + 1, iToAdd + random.nextDouble()));
			cloudletList.add(cloudlet);
			PSDatacenterBroker.getCloudletMap().put(cloudlet, publisher);
			PSDatacenterBroker.getCloudletPubMap().put(cloudlet, new ArrayList<PSCloudlet>());
			cloudletList.add(iToAdd, cloudlet);
		}

		// System.out.println(distLoad.getMean() + "\t" + cloudletList.size() +
		// "\t"
		// + ((double) ExperimentConstants.SIMULATION_INTERVAL *
		// publisher.getRate()));

		return cloudletList;
	}

	public void setupExperiment(String outputFileName, int num_user,
			Class<? extends DatacenterBroker> datacenterBrokerClass) {
		try {
			FileOutputStream fw = new FileOutputStream(outputFileName, false);
			Log.setOutput(fw);
		} catch (IOException e) {
			Log.printLine("Experiment failed: File " + outputFileName + " not found");
			e.printStackTrace();
			System.exit(-1);
		}

		Logger.getRootLogger().setLevel(Level.INFO);
		Log.disable();

		// Log.setOutput(System.out);
		// Log.enable();

		Log.printLine("Starting Experiment...");

		try {
			// Initialize the CloudSim package. It should be called
			// before creating any entities.
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag, SimulationConstants.MINIMUM_TIME_BETWEEN_EVENTS);

			// Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			datacenter = Helper.createDatacenter("Datacenter");

			// Create Broker
			broker = (PSDatacenterBroker) Helper.createBroker(datacenterBrokerClass);
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	protected void setCloudletList(List<Cloudlet> cloudlets) {
		cloudletList.clear();
		cloudletList.addAll(cloudlets);
	}

	protected void setVmList(VmConfiguration vmConfig, int initialNumOfVms) {
		vmlist = Helper.createVmList(vmConfig, initialNumOfVms, broker.getId());

		// submit vm list to the broker
		broker.submitVmList(vmlist);

		setCloudletUser(broker.getId());

		// submit cloudlet list to the broker
		broker.submitCloudletList(cloudletList);
	}

	protected void setCloudletUser(int userId) {
		for (Cloudlet cloudlet : cloudletList) {
			cloudlet.setUserId(userId);
		}
	}

	protected void updateResults(String metric, double value) {
		if (!resultsMap.keySet().contains(metric))
			resultsMap.put(metric, new LinkedList<Double>());

		resultsMap.get(metric).add(value);

		System.out.println(this.getClass().getName() + ": " + metric + "\t" + value);
	}

	public void runExperiment() {
		CloudSim.terminateSimulation(SimulationConstants.SIMULATION_INTERVAL);

		CloudSim.startSimulation();

		// Print results when simulation is over
		// List<Cloudlet> newList = broker.getCloudletReceivedList();
		// Helper.printCloudletList(newList);
	}

	/**
	 * Creates main() to run this example.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < SimulationConstants.PUBLISH_RATE.length; i++) {
			double rtSum = 0.0;
			double DprocSum = 0.0;
			double DtranSum = 0.0;
			for (int k = 0; k < SimulationConstants.EXPERIMENT_RUNS; k++) {
				try {
					PSExperiment experiment = new PSExperiment();
					Publisher publisher = new Publisher(SimulationConstants.TOPIC, SimulationConstants.PUBLISH_RATE[i]);
					Subscriber subscriber = new Subscriber(publisher.getTopic());
					PSNetworkRegistry.addSubscriber(subscriber);
					SubscriberRegistry.addSubscriber(subscriber);

					experiment.setupExperiment(
							"results/ps" + SimulationConstants.PUBLISH_RATE[i] + "" + (k + 1) + ".txt", 1,
							PSDatacenterBroker.class);

					List<Cloudlet> cloudletList = experiment.createCloudletList(publisher);

					experiment.setCloudletList(cloudletList);

					// Create virtual machines according with Amazon EC2
					// m3.medium
					// type
					VmConfiguration vmConfig = new VmConfiguration(SimulationConstants.BROKER_VM_MIPS,
							SimulationConstants.BROKER_VM_NUMBER_OF_PES, SimulationConstants.BROKER_VM_RAM,
							SimulationConstants.BROKER_VM_BW, SimulationConstants.BROKER_VM_SIZE,
							SimulationConstants.BROKER_VM_VMM, PSCloudletSchedulerSpaceShared.class);

					experiment.setVmList(vmConfig, 1);

					experiment.runExperiment();

					((PSDatacenterBroker) experiment.broker).calculateAndPrintResults();

					DprocSum += PSDatacenterBroker.getAvgDproc();
					DtranSum += PSDatacenterBroker.getAvgDtran();
					rtSum += PSDatacenterBroker.getAvgRT();
					logger.debug("Valid run #" + (k + 1) + "\t" + SimulationConstants.PUBLISH_RATE[i] + "\t"
							+ PSDatacenterBroker.getAvgDproc() + "\t" + PSDatacenterBroker.getAvgDtran() + "\t"
							+ PSDatacenterBroker.getAvgtON() + "\t" + PSDatacenterBroker.getAvgtOFF() + "\t"
							+ PSDatacenterBroker.getAvgRT());
				} catch (Exception e) {
					logger.debug("Exception\t");
					// e.printStackTrace();
					// Invalid run. It should be performed again
					k--;
					// System.exit(-1);
				}
			}
			logger.info(SimulationConstants.PUBLISH_RATE[i] + "\t" + (DprocSum / SimulationConstants.EXPERIMENT_RUNS)
					+ "\t" + (DtranSum / SimulationConstants.EXPERIMENT_RUNS) + "\t" + "\t"
					+ SimulationConstants.SUBSCRIBE_ON_RATE + "\t" + SimulationConstants.SUBSCRIBE_OFF_RATE + "\t"
					+ (rtSum / SimulationConstants.EXPERIMENT_RUNS));
		}
	}

}