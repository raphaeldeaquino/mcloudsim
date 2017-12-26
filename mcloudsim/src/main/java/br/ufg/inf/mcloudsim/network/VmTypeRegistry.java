package br.ufg.inf.mcloudsim.network;

import java.util.HashMap;
import java.util.Map;

import br.ufg.inf.mcloudsim.core.Consts;
import br.ufg.inf.mcloudsim.simulator.VmConfiguration;

public class VmTypeRegistry {

	private static VmTypeRegistry instance;

	private Map<String, VmConfiguration> vmTypeMap;

	private VmTypeRegistry() {
		vmTypeMap = new HashMap<>();
	}

	public static VmTypeRegistry getInstance() {
		if (instance == null)
			instance = new VmTypeRegistry();

		return instance;
	}

	public void addVmType(String id, int cpuCores, double cpuClock, double ram, double storage, double bw, String provider, String region, double price) {
		if (id == null || id.isEmpty() || vmTypeMap.containsKey(id))
			throw new IllegalArgumentException();

		double mips = (cpuClock * Consts.GHZ * Consts.INSTRUCTIONS_PER_CICLE) / Consts.MILLION;
		VmConfiguration vmConfiguration = new VmConfiguration(mips, cpuCores, ram, bw, storage, id, provider, region,
				price);

		vmTypeMap.put(id, vmConfiguration);
	}

	public boolean containVmType(String id) {
		if (id == null || id.isEmpty())
			return false;

		return this.vmTypeMap.containsKey(id);
	}

	public VmConfiguration getVmType(String id) {
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException();

		return vmTypeMap.get(id);
	}
}
