package br.ufg.inf.mcloudsim.simulator;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

// OK
public class PSHost extends Host {

	public PSHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
	}

	// OK
	public double updateVmsTransmission(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (Vm vm : getVmList()) {
			double time = ((VmType) vm).updateVmTransmission(currentTime,
					((PSVmSchedulerSpaceShared) getVmScheduler()).getAllocatedBWForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

}
