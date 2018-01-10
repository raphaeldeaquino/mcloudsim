package br.ufg.inf.mcloudsim.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import br.ufg.inf.mcloudsim.network.DeployablePathNode;
import br.ufg.inf.mcloudsim.network.PSNetworkDescriptor;
import br.ufg.inf.mcloudsim.network.PSNetworkPath;
import br.ufg.inf.mcloudsim.network.ResourceSynthesisResult;
import br.ufg.inf.mcloudsim.network.VmTypeRegistry;

public class PSNetworkCreator {

	public static final double DATACENTER_SCHED_INTERVAL = 0.0;

	private static int vmCount = 0;

	private static Map<String, Vm> firstVmMap = new HashMap<>();

	private static Map<String, Vm> nodeToVmMap = new HashMap<>();

	private static Map<Integer, String> vmToNodeMap = new HashMap<>();

	/**
	 * Creates a list of hosts
	 * 
	 * @param numHosts
	 *            how many hosts to be created
	 * @return the list of hosts
	 * @throws SimulationCreationException
	 */
	public static List<Host> createHostList(int numHosts) throws SimulationCreationException {
		List<Host> hostList = new LinkedList<Host>();
		for (int i = 0; i < numHosts; i++) {
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < SimulationConstants.HOST_NUMBER_OF_PES; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(SimulationConstants.HOST_MIPS)));
			}

			try {
				hostList.add(new PSHost(i, new RamProvisionerSimple(SimulationConstants.HOST_RAM),
						new BwProvisionerSimple(SimulationConstants.HOST_BW), SimulationConstants.HOST_STORAGE, peList,
						SimulationConstants.BROKER_VM_SCHEDULER.getConstructor(List.class).newInstance(peList)));
			} catch (Exception e) {
				throw new SimulationCreationException(e.getMessage(), e);
			}
		}
		return hostList;
	}

	/**
	 * Creates a datacenter
	 * 
	 * @return the datacenter
	 * @throws SimulationCreationException
	 */
	public static Datacenter createDatacenter(ResourceSynthesisResult resourceSynthesisResult)
			throws SimulationCreationException {
		// Here are the steps needed to create a Datacenter:
		// We need to create a list to store our machine
		List<Host> hostList = createHostList(SimulationConstants.NUM_HOST);

		// Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located

		// the cost of using processing in this resource
		double cost = SimulationConstants.BROKER_VM_COST_PER_PROCESSING;

		// the cost of using memory in this resource
		double costPerMem = SimulationConstants.BROKER_VM_COST_PER_MEMORY;

		// the cost of using storage in this resource
		double costPerStorage = SimulationConstants.BROKER_VM_COST_PER_STORAGE;

		// the cost of using bw in this resource
		double costPerBw = 0.0;

		// we are not adding SAN devices by now
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		String provider = resourceSynthesisResult.getProvider();

		// We need to create a Datacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new PSDatacenter(provider, characteristics, new VmAllocationPolicySimple(hostList), storageList,
					DATACENTER_SCHED_INTERVAL);
		} catch (Exception e) {
			throw new SimulationCreationException(e.getMessage(), e);
		}

		return datacenter;
	}

	/**
	 * Creates a list of VMs
	 * 
	 * @param psNetwork
	 * @param resourceSynthesisResult
	 * @return the list of VMs
	 * @throws SimulationCreationException
	 */
	public static List<Vm> createVmList(PSNetworkDescriptor psNetwork, ResourceSynthesisResult resourceSynthesisResult,
			PSDatacenterBroker datacenterBroker) throws SimulationCreationException {
		List<Vm> vms = new LinkedList<Vm>();
		List<PSNetworkPath> psNetworkPaths = psNetwork.getPaths();
		List<String> deployedNodes = new LinkedList<>();
		VmTypeRegistry vmTypeRegistry = VmTypeRegistry.getInstance();

		for (PSNetworkPath psNetworkPath : psNetworkPaths) {
			LinkedList<DeployablePathNode> brokersPath = psNetworkPath.getBrokersPath();
			boolean firstVm = true;
			for (DeployablePathNode brokerNode : brokersPath) {
				String nodeId = brokerNode.getTargetNode().getId();

				if (deployedNodes.contains(nodeId))
					throw new SimulationCreationException("Broker " + nodeId + " already deployed");

				String vmId = resourceSynthesisResult.getVmForBroker(nodeId);
				VmConfiguration vmDescriptor = vmTypeRegistry.getVmType(vmId);

				if (vmDescriptor == null)
					throw new SimulationCreationException("VM descriptor for type " + vmId + " not found");

				CloudletScheduler cloudletScheduler = new PSCloudletSchedulerSpaceShared(psNetworkPath, nodeId);
				Vm vm = new VmType(vmDescriptor.getName(), ++vmCount, datacenterBroker.getId(), vmDescriptor.getMips(),
						vmDescriptor.getNumberOfPes(), vmDescriptor.getRam(), vmDescriptor.getBw(),
						vmDescriptor.getSize(), cloudletScheduler, vmDescriptor.getProvider(), vmDescriptor.getRegion(),
						vmDescriptor.getPrice());

				vms.add(vm);
				datacenterBroker.addVmMap(nodeId, vm.getId());

				if (firstVm) {
					firstVmMap.put(psNetworkPath.getPathId(), vm);
					firstVm = false;
				}
				nodeToVmMap.put(nodeId, vm);
				vmToNodeMap.put(vm.getId(), nodeId);
				deployedNodes.add(nodeId);
			}
		}

		return vms;
	}

	public static Vm getFirstVmOfPath(String pubId) {
		if (pubId == null || pubId.isEmpty())
			throw new IllegalArgumentException("Invalid publisher ID");

		return firstVmMap.get(pubId);
	}

	public static void reset() {
		vmCount = 0;
		firstVmMap = new HashMap<>();
		nodeToVmMap = new HashMap<>();
		vmToNodeMap = new HashMap<>();
	}
}
