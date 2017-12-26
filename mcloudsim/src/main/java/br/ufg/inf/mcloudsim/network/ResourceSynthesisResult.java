package br.ufg.inf.mcloudsim.network;

import java.util.HashMap;
import java.util.Map;

public class ResourceSynthesisResult {

	private String provider;
	private String region;
	private Map<String, String> resultMap;
	private VmTypeRegistry vmTypeRegistry;

	public ResourceSynthesisResult(String provider, String region) {
		super();
		this.provider = provider;
		this.region = region;
		this.resultMap = new HashMap<>();
		this.vmTypeRegistry = VmTypeRegistry.getInstance();
	}

	public void addResultPair(String brokerId, String vmTypeId) {
		if (brokerId == null || brokerId.isEmpty() || vmTypeId == null || vmTypeId.isEmpty()
				|| !this.vmTypeRegistry.containVmType(vmTypeId))
			throw new IllegalArgumentException();

		this.resultMap.put(brokerId, vmTypeId);
	}

	public String getVmForBroker(String brokerId) {
		if (brokerId == null || brokerId.isEmpty())
			throw new IllegalArgumentException();

		return this.resultMap.get(brokerId);
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
	
	@Override
	public String toString() {
		return "Result : provider=" + provider + "; region=" + region + "; mapping=" + resultMap;
	}
}
