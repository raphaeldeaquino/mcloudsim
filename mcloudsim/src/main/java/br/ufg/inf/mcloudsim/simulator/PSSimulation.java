package br.ufg.inf.mcloudsim.simulator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import br.ufg.inf.mcloudsim.core.EventEntity;
import br.ufg.inf.mcloudsim.core.PSNetworkRegistry;
import br.ufg.inf.mcloudsim.core.Publisher;
import br.ufg.inf.mcloudsim.core.Subscriber;
import br.ufg.inf.mcloudsim.network.DeployablePathNode;
import br.ufg.inf.mcloudsim.network.PSNetworkDescriptor;
import br.ufg.inf.mcloudsim.network.PSNetworkPath;
import br.ufg.inf.mcloudsim.network.ResourceSynthesisResult;

public class PSSimulation {

	public static final String TOPIC = "TOPIC.";

	private static Logger logger = Logger.getLogger(PSSimulation.class.getName());

	public static final int NUM_USER = 1;

	protected static int cloudletCount;

	private PSDatacenterBroker datacenterBroker;

	public PSSimulation() {
		cloudletCount = 0;
		EventEntity.reset();
		PSNetworkRegistry.reset();
	}

	public void setupSimulation(String outputFileName, Class<? extends DatacenterBroker> datacenterBrokerClass,
			PSNetworkDescriptor psNetwork, ResourceSynthesisResult resourceSynthesisResult, Double rate)
			throws SimulationCreationException {
		try {
			FileOutputStream fw = new FileOutputStream(outputFileName, false);
			Log.setOutput(fw);
		} catch (IOException e) {
			Log.printLine("Experiment failed: File " + outputFileName + " not found");
			throw new SimulationCreationException("Experiment failed: File " + outputFileName + " not found", e);
		}

		Logger.getRootLogger().setLevel(Level.INFO);
		Log.disable();

		Logger.getRootLogger().setLevel(Level.DEBUG);
		Log.setOutput(System.out);
		Log.enable();

		Log.printLine("Starting Experiment...");

		// Initialize the CloudSim package. It should be called
		// before creating any entities.
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = true; // mean trace events

		// Initialize the CloudSim library
		CloudSim.init(NUM_USER, calendar, trace_flag, SimulationConstants.MINIMUM_TIME_BETWEEN_EVENTS);

		// Create Datacenters
		// Datacenters are the resource providers in CloudSim. We need at
		// list one of them to run a CloudSim simulation
		PSNetworkCreator.createDatacenter(resourceSynthesisResult);

		try {
			datacenterBroker = new PSDatacenterBroker(resourceSynthesisResult.getProvider(), psNetwork);
			List<Vm> vmlist = PSNetworkCreator.createVmList(psNetwork, resourceSynthesisResult, datacenterBroker);
			List<PSNetworkPath> networkPaths = psNetwork.getPaths();

			// submit vm list to the broker
			datacenterBroker.submitVmList(vmlist);

			for (PSNetworkPath networkPath : networkPaths) {
				Publisher publisher = networkPath.getPublisher();
				publisher.setRate(rate);
				Subscriber subscriber = networkPath.getSubscriber();

				// Register entities
				PSNetworkRegistry.addPublisher(publisher);
				PSNetworkRegistry.addSubscriber(subscriber);

				List<Cloudlet> cloudletList = createCloudletList(publisher, datacenterBroker.getId(), networkPath);

				// Submit cloudlet list to the broker
				datacenterBroker.submitCloudletList(cloudletList);
			}
		} catch (Exception e) {
			throw new SimulationCreationException(e.getMessage(), e);
		}
	}

	public void runSimulation() {
		CloudSim.terminateSimulation(SimulationConstants.SIMULATION_INTERVAL);

		CloudSim.startSimulation();

		// Print results when simulation is over
		// List<Cloudlet> newList = broker.getCloudletReceivedList();
		// Helper.printCloudletList(newList);
	}

	public double getAvgDprOfBrokerInPath(String pathId, String brokerId) {
		Map<String, Double> avgDPrMapOfPath = datacenterBroker.getAvgDPrMapOfPath(pathId);
		return avgDPrMapOfPath.containsKey(brokerId) ? avgDPrMapOfPath.get(brokerId) : Double.NaN;
	}

	public double getAvgDtrOfBrokerInPath(String pathId, String brokerId) {
		Map<String, Double> avgDTrMapOfPath = datacenterBroker.getAvgDTrMapOfPath(pathId);
		return avgDTrMapOfPath.containsKey(brokerId) ? avgDTrMapOfPath.get(brokerId) : Double.NaN;
	}

	public double getAvgRTOfBrokerInPath(String pathId, String brokerId) {
		Map<String, Double> avgRTMapOfPath = datacenterBroker.getAvgRTMapOfPath(pathId);
		return avgRTMapOfPath.containsKey(brokerId) ? avgRTMapOfPath.get(brokerId) : Double.NaN;
	}

	public double getAvgRTOfPath(String pathId) {
		return datacenterBroker.getAvgRTOfPath(pathId);
	}

	public double getAvgTON(String pathId) {
		return datacenterBroker.getAvgTOnOfPath(pathId);
	}

	public double getAvgTOFF(String pathId) {
		return datacenterBroker.getAvgTOFFOfPath(pathId);
	}

	protected List<Cloudlet> createCloudletList(Publisher publisher, int userId, PSNetworkPath networkPath) {
		PoissonDistribution distLoad = new PoissonDistribution(publisher.getRate());
		Random random = new Random(System.currentTimeMillis());
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		Vm vm = PSNetworkCreator.getFirstVmOfPath(publisher.getId());
		String brokerId = networkPath.getBrokersPath().getFirst().getTargetNode().getId();

		// For each second of simulation submit 'rate' requests
		for (int time = 1; time <= SimulationConstants.SIMULATION_INTERVAL; time++) {
			// How many events for this iteration
			int nEvents = distLoad.sample();

			// Events for this iteration
			while (nEvents >= 1) {
				double arrivalTime = Math.min(time, (time - 1) + random.nextDouble());
				PSCloudlet cloudlet = createCloudlet(publisher, userId, arrivalTime, vm, brokerId);
				cloudletList.add(cloudlet);
				nEvents--;
			}
		}

		while (cloudletList.size() > (double) SimulationConstants.SIMULATION_INTERVAL * publisher.getRate()) {
			int iToRemove = random.nextInt(cloudletList.size());
			cloudletList.remove(iToRemove);
		}

		while (cloudletList.size() < (double) SimulationConstants.SIMULATION_INTERVAL * publisher.getRate()) {
			int iToAdd = cloudletList.isEmpty() ? 0 : random.nextInt(cloudletList.size());
			double arrivalTime = Math.min(iToAdd + 1, iToAdd + random.nextDouble());
			PSCloudlet cloudlet = createCloudlet(publisher, userId, arrivalTime, vm, brokerId);
			cloudletList.add(iToAdd, cloudlet);
		}

		// System.out.println(distLoad.getMean() + "\t" + cloudletList.size() +
		// "\t"
		// + ((double) ExperimentConstants.SIMULATION_INTERVAL *
		// publisher.getRate()));

		return cloudletList;
	}

	private PSCloudlet createCloudlet(Publisher publisher, int userId, double arrivalTime, Vm vm, String brokerId) {
		PSCloudlet cloudlet = new PSCloudlet(++cloudletCount, publisher.getId(), brokerId, (long) publisher.getMIpr(),
				(long) publisher.getBtr(), 1, SimulationConstants.PUBLISH_CLOUDLET_FILESIZE,
				SimulationConstants.CLOUDLET_OUTPUT_SIZE, new UtilizationModelFull(), new UtilizationModelFull(),
				new UtilizationModelFull(), PSCloudlet.cloudletType.IN, arrivalTime);
		cloudlet.setUserId(userId);
		cloudlet.setVmId(vm.getId());

		return cloudlet;
	}

	private static String calculateAndFormatStas(Map<String, Double> mapSum, int n) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iterator = mapSum.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next();
			sb.append(key);
			sb.append("=");
			double avg = mapSum.get(key) / (double) n;
			sb.append(String.format("%.4f", avg));

			if (iterator.hasNext())
				sb.append(", ");
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		PSNetworkDescriptor psNetwork = null;
		List<ResourceSynthesisResult> resourceSynthesisResult = null;
		try {
			psNetwork = XmlReader.readNetworkDescriptor(SimulationConstants.PS_NETWORK_DESCRIPTOR);
			resourceSynthesisResult = XmlReader.readResourceSynthesisResults(SimulationConstants.PS_NETWORK_DESCRIPTOR);
		} catch (XmlParserException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		// Using a single path
		for (int i = 0; i < SimulationConstants.PUBLISH_RATE.length; i++) {
			Map<String, Double> DprSum = new HashMap<>();
			Map<String, Double> DtrSum = new HashMap<>();
			Map<String, Double> brokerRTSum = new HashMap<>();
			double rtSum = 0.0;

			for (int k = 0; k < SimulationConstants.EXPERIMENT_RUNS; k++) {
				try {
					PSSimulation simulation = new PSSimulation();

					simulation.setupSimulation(
							"results/ps" + SimulationConstants.PUBLISH_RATE[i] + "" + (k + 1) + ".txt",
							PSDatacenterBroker.class, psNetwork, resourceSynthesisResult.get(0),
							SimulationConstants.PUBLISH_RATE[i]);

					simulation.runSimulation();

					List<PSNetworkPath> networkPaths = psNetwork.getPaths();
					String singlePath = null;

					for (PSNetworkPath networkPath : networkPaths) {
						String pathId = networkPath.getPathId();
						singlePath = pathId;
						LinkedList<DeployablePathNode> brokers = networkPath.getBrokersPath();
						double rt = simulation.getAvgRTOfPath(pathId);

						for (DeployablePathNode brokerNode : brokers) {
							String brokerId = brokerNode.getTargetNode().getId();
							double Dpr = simulation.getAvgDprOfBrokerInPath(pathId, brokerId);
							double Dtr = simulation.getAvgDtrOfBrokerInPath(pathId, brokerId);
							double brokerRT = simulation.getAvgRTOfBrokerInPath(pathId, brokerId);

							if (!DprSum.containsKey(brokerId))
								DprSum.put(brokerId, Dpr);
							else
								DprSum.put(brokerId, DprSum.get(brokerId) + Dpr);
							if (!DtrSum.containsKey(brokerId))
								DtrSum.put(brokerId, Dtr);
							else
								DtrSum.put(brokerId, DtrSum.get(brokerId) + Dtr);
							if (!brokerRTSum.containsKey(brokerId))
								brokerRTSum.put(brokerId, brokerRT);
							else
								brokerRTSum.put(brokerId, brokerRTSum.get(brokerId) + brokerRT);
						}

						rtSum += Double.isNaN(rt) ? 0.0 : rt;
					}
					logger.debug("Valid run #" + (k + 1) + "\t" + SimulationConstants.PUBLISH_RATE[i] + "\t"
							+ calculateAndFormatStas(DprSum, k + 1) + "\t" + calculateAndFormatStas(DtrSum, k + 1)
							+ "\t" + simulation.getAvgTON(singlePath) + "\t" + simulation.getAvgTOFF(singlePath) + "\t"
							+ (rtSum != 0.0 ? rtSum / (k + 1) : Double.NaN));
				} catch (Exception e) {
					logger.debug("Exception\t");
					e.printStackTrace();
					// Invalid run. It should be performed again
					k--;
					// System.exit(-1);
				}
			}
			logger.info(SimulationConstants.PUBLISH_RATE[i] + "\t"
					+ calculateAndFormatStas(DprSum, SimulationConstants.EXPERIMENT_RUNS) + "\t"
					+ calculateAndFormatStas(DtrSum, SimulationConstants.EXPERIMENT_RUNS) + "\t"
					+ (rtSum != 0.0 ? rtSum / SimulationConstants.EXPERIMENT_RUNS : Double.NaN));
		}
	}
}
