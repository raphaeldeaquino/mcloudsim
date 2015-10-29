/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import br.ufg.inf.mcloudsim.core.PSBroker;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.simulator.PSCloudlet;
import br.ufg.inf.mcloudsim.simulator.PSCloudletSchedulerSpaceShared;
import br.ufg.inf.mcloudsim.simulator.PSDatacenterBroker;
import br.ufg.inf.mcloudsim.simulator.VmConfiguration;
import br.ufg.inf.mcloudsim.utils.Helper;

/**
 * Simulates a pub/sub broker with subscribers connectivity distribution
 * 
 * @author Raphael Gomes
 *
 */
public class PSExperiment {

	public static int cloudletCount;

	protected Datacenter datacenter;

	protected PSDatacenterBroker broker;

	protected List<Vm> vmlist;

	protected List<Cloudlet> cloudletList = new LinkedList<Cloudlet>();

	protected Map<String, List<Double>> resultsMap = new HashMap<String, List<Double>>();

	public static int c = 1;

	public PSExperiment() {
		cloudletCount = 0;
	}

	protected int getBrokerId() {
		return broker.getId();
	}

	/*
	 * protected int getDatacenterId() { return datacenter.getId(); }
	 */

	protected List<Cloudlet> createCloudletList(Publisher publisher) {
		Random random = new Random();
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		double nEvents = publisher.getRate();

		// For each second of simulation submit 'rate' requests
		for (int time = 1; time <= ExperimentConstants.SIMULATION_INTERVAL; time++) {
			while (nEvents >= 1.0) {
				PSCloudlet cloudlet = new PSCloudlet(++cloudletCount,
						(long) Math.ceil(ExperimentConstants.SERVICE_TIME_IN * ExperimentConstants.BROKER_VM_MIPS), 1,
						ExperimentConstants.PUBLISH_CLOUDLET_FILESIZE, ExperimentConstants.CLOUDLET_OUTPUT_SIZE,
						new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(),
						PSCloudlet.cloudletType.PUBLISH, Math.min(time, (time - 1) + random.nextDouble()));
				cloudletList.add(cloudlet);
				PSDatacenterBroker.getCloudletMap().put(cloudlet, publisher);
				PSDatacenterBroker.getCloudletPubMap().put(cloudlet, new ArrayList<PSCloudlet>());
				nEvents--;
			}
			nEvents += publisher.getRate();
		}

		return cloudletList;
	}

	public void setupExperiment(String outputFileName, int num_user,
			Class<? extends DatacenterBroker> datacenterBrokerClass, boolean isPrivateDatacenter) {
		try {
			Log.setOutput(new FileOutputStream(new File(outputFileName)));
		} catch (FileNotFoundException e1) {
			Log.printLine("Experiment failed: File " + outputFileName + " not found");
			e1.printStackTrace();
		}
		
		Log.printLine("Starting Experiment...");

		try {
			// Initialize the CloudSim package. It should be called
			// before creating any entities.
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag, 0.01);

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
		// Starts the simulation
		// TODO: An exception can't be throws here
		try {
			CloudSim.startSimulation();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		CloudSim.stopSimulation();

		// Print results when simulation is over
		List<Cloudlet> newList = broker.getCloudletReceivedList();
		Helper.printCloudletList(newList);
	}

	/**
	 * Creates main() to run this example.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < ExperimentConstants.PUBLISH_RATE.length; i++) {
			double rtSum = 0.0;
			for (int k = 0; k < ExperimentConstants.EXPERIMENT_RUNS; k++) {
				PSExperiment experiment = new PSExperiment();
				Publisher publisher = new Publisher(ExperimentConstants.TOPIC, ExperimentConstants.PUBLISH_RATE[i]);
				for (int s = 0; s < ExperimentConstants.SUBSCRIBE_NUMBER; s++) {
					Subscriber subscriber = new Subscriber(publisher.getTopic());
					PSBroker.addSubscriber(subscriber);
				}

				experiment.setupExperiment("results/ps" + ExperimentConstants.PUBLISH_RATE[i] + "" + c + ".txt", 1,
						PSDatacenterBroker.class, false);

				List<Cloudlet> cloudletList = experiment.createCloudletList(publisher);

				experiment.setCloudletList(cloudletList);

				// Create virtual machines according with Amazon EC2 m3.medium
				// type
				VmConfiguration vmConfig = new VmConfiguration(ExperimentConstants.BROKER_VM_MIPS,
						ExperimentConstants.BROKER_VM_NUMBER_OF_PES, ExperimentConstants.BROKER_VM_RAM,
						ExperimentConstants.BROKER_VM_BW, ExperimentConstants.BROKER_VM_SIZE,
						ExperimentConstants.BROKER_VM_VMM, new PSCloudletSchedulerSpaceShared());

				experiment.setVmList(vmConfig, 1);

				try {
					experiment.runExperiment();

					((PSDatacenterBroker) experiment.broker).printRT();

					if (Math.abs(PSDatacenterBroker.getAvgtON()
							- 1.0 / ExperimentConstants.SUBSCRIBE_ON_OFF_RATE) <= ExperimentConstants.SUBSCRIBE_ON_OFF_DIFF
							&& Math.abs(PSDatacenterBroker.getAvgtOFF() - 1.0
									/ ExperimentConstants.SUBSCRIBE_ON_OFF_RATE) <= ExperimentConstants.SUBSCRIBE_ON_OFF_DIFF) {
						rtSum += PSDatacenterBroker.getAvgRT();
						System.out.print("Valid run #" + (k + 1) + "\t");
					} else {
						// Invalid run. It should be performed again
						k--;
						System.out.print("Invalid run\t");
					}
				} catch (IndexOutOfBoundsException e) {
					System.err.print("Exception\t");
					// Invalid run. It should be performed again
					k--;
				}
				c++;
				PSBroker.resetBroker();
				System.out.println(PSDatacenterBroker.getAvgRT() + "\t" + PSDatacenterBroker.getAvgtON() + "\t"
						+ PSDatacenterBroker.getAvgtOFF());
			}
			System.out.println(rtSum / ExperimentConstants.EXPERIMENT_RUNS);
		}
	}

}