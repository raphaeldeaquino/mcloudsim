package br.ufg.inf.mcloudsim.simulator;

import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;

// OK
public class PSVmSchedulerSpaceShared extends VmSchedulerSpaceShared {

	public PSVmSchedulerSpaceShared(List<? extends Pe> pelist) {
		super(pelist);
	}
	
	public Double getAllocatedBWForVm(Vm vm) {
		return (double) vm.getBw();
	}

}
