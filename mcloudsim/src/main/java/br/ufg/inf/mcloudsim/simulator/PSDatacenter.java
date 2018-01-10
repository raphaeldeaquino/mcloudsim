package br.ufg.inf.mcloudsim.simulator;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

// OK
public class PSDatacenter extends Datacenter {

	/** The last time some cloudlet was transmitted in the datacenter. */
	private double lastTransmissionTime;

	public PSDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList, double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
	}

	public double getLastTransmissionTime() {
		return lastTransmissionTime;
	}

	public void setLastTransmissionTime(double lastTransmissionTime) {
		this.lastTransmissionTime = lastTransmissionTime;
	}

	@Override
	public void processEvent(SimEvent ev) {
		int srcId = -1;

		switch (ev.getTag()) {
		// Resource characteristics inquiry
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			srcId = ((Integer) ev.getData()).intValue();
			sendNow(srcId, ev.getTag(), getCharacteristics());
			break;

		// Resource dynamic info inquiry
		case CloudSimTags.RESOURCE_DYNAMICS:
			srcId = ((Integer) ev.getData()).intValue();
			sendNow(srcId, ev.getTag(), 0);
			break;

		case CloudSimTags.RESOURCE_NUM_PE:
			srcId = ((Integer) ev.getData()).intValue();
			int numPE = getCharacteristics().getNumberOfPes();
			sendNow(srcId, ev.getTag(), numPE);
			break;

		case CloudSimTags.RESOURCE_NUM_FREE_PE:
			srcId = ((Integer) ev.getData()).intValue();
			int freePesNumber = getCharacteristics().getNumberOfFreePes();
			sendNow(srcId, ev.getTag(), freePesNumber);
			break;

		// New Cloudlet arrives
		case CloudSimTags.CLOUDLET_SUBMIT:
			processCloudletSubmit(ev, false);
			break;

		// New Cloudlet arrives
		case PSCloudSimTags.CLOUDLET_TRANSMISSION_START:
			processCloudletStartTransmission(ev);
			break;

		// New Cloudlet arrives, but the sender asks for an ack
		case CloudSimTags.CLOUDLET_SUBMIT_ACK:
			processCloudletSubmit(ev, true);
			break;

		// Cancels a previously submitted Cloudlet
		case CloudSimTags.CLOUDLET_CANCEL:
			processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
			break;

		// Pauses a previously submitted Cloudlet
		case CloudSimTags.CLOUDLET_PAUSE:
			processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
			break;

		// Pauses a previously submitted Cloudlet
		case PSCloudSimTags.CLOUDLET_TRANSMISSION_PAUSE:
			processCloudletTransmission(ev, PSCloudSimTags.CLOUDLET_TRANSMISSION_PAUSE);
			break;

		// Pauses a previously submitted Cloudlet, but the sender
		// asks for an acknowledgement
		case CloudSimTags.CLOUDLET_PAUSE_ACK:
			processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
			break;

		// Resumes a previously submitted Cloudlet
		case CloudSimTags.CLOUDLET_RESUME:
			processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
			break;

		// Pauses a previously submitted Cloudlet
		case PSCloudSimTags.CLOUDLET_TRANSMISSION_RESUME:
			processCloudletTransmission(ev, PSCloudSimTags.CLOUDLET_TRANSMISSION_RESUME);
			break;

		// Resumes a previously submitted Cloudlet, but the sender
		// asks for an acknowledgement
		case CloudSimTags.CLOUDLET_RESUME_ACK:
			processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
			break;

		// Moves a previously submitted Cloudlet to a different resource
		case CloudSimTags.CLOUDLET_MOVE:
			processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
			break;

		// Moves a previously submitted Cloudlet to a different resource
		case CloudSimTags.CLOUDLET_MOVE_ACK:
			processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
			break;

		// Checks the status of a Cloudlet
		case CloudSimTags.CLOUDLET_STATUS:
			processCloudletStatus(ev);
			break;

		// Ping packet
		case CloudSimTags.INFOPKT_SUBMIT:
			processPingRequest(ev);
			break;

		case CloudSimTags.VM_CREATE:
			processVmCreate(ev, false);
			break;

		case CloudSimTags.VM_CREATE_ACK:
			processVmCreate(ev, true);
			break;

		case CloudSimTags.VM_DESTROY:
			processVmDestroy(ev, false);
			break;

		case CloudSimTags.VM_DESTROY_ACK:
			processVmDestroy(ev, true);
			break;

		case CloudSimTags.VM_MIGRATE:
			processVmMigrate(ev, false);
			break;

		case CloudSimTags.VM_MIGRATE_ACK:
			processVmMigrate(ev, true);
			break;

		case CloudSimTags.VM_DATA_ADD:
			processDataAdd(ev, false);
			break;

		case CloudSimTags.VM_DATA_ADD_ACK:
			processDataAdd(ev, true);
			break;

		case CloudSimTags.VM_DATA_DEL:
			processDataDelete(ev, false);
			break;

		case CloudSimTags.VM_DATA_DEL_ACK:
			processDataDelete(ev, true);
			break;

		case CloudSimTags.VM_DATACENTER_EVENT:
			updateCloudletProcessing();
			checkCloudletCompletion();
			break;
			
		case PSCloudSimTags.VM_DATACENTER_EVENT_TRANSMISSION:
			updateCloudletTransmission();
			checkCloudletTransmissionCompletion();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev
	 *            information about the event just happened
	 * @param ack
	 *            indicates if the event's sender expects to receive an
	 *            acknowledge message when the event finishes to be processed
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	// OK
	protected void processCloudletStartTransmission(SimEvent ev) {
		updateCloudletTransmission();

		try {
			// gets the Cloudlet object
			PSCloudlet cl = (PSCloudlet) ev.getData();

			// checks whether this Cloudlet has finished or not
			if (cl.isTransmitted()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printConcatLine(getName(), ": Warning - Cloudlet #", cl.getCloudletId(), " owned by ", name,
						" is already transmitted.");
				Log.printLine("Therefore, it is not being transmitted again");
				Log.printLine();

				Log.printConcatLine(CloudSim.clock(), ": cloudlet ", cl, " finished");
				sendNow(cl.getUserId(), PSCloudSimTags.CLOUDLET_TRANSMITTED, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(),
					getCharacteristics().getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();

			Host host = getVmAllocationPolicy().getHost(vmId, userId);
			Vm vm = host.getVm(vmId, userId);
			PSCloudletSchedulerSpaceShared scheduler = (PSCloudletSchedulerSpaceShared) vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletTransmit(cl);

			// if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime))
				send(getId(), estimatedFinishTime, PSCloudSimTags.VM_DATACENTER_EVENT_TRANSMISSION);

		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

		checkCloudletTransmissionCompletion();
	}

	/**
	 * Processes a Cloudlet based on the event type.
	 * 
	 * @param ev
	 *            information about the event just happened
	 * @param type
	 *            event type
	 * 
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	// OK
	protected void processCloudletTransmission(SimEvent ev, int type) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;

		try { // if the sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				vmId = cl.getVmId();
			} catch (Exception e) {
				Log.printConcatLine(super.getName(), ": Error in processing Cloudlet");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printConcatLine(super.getName(), ": Error in processing a Cloudlet.");
			Log.printLine(e.getMessage());
			return;
		}

		// begins executing ....
		if (type == PSCloudSimTags.CLOUDLET_TRANSMISSION_PAUSE)
			processCloudletTransmissionPause(cloudletId, userId, vmId);
		else if (type == PSCloudSimTags.CLOUDLET_TRANSMISSION_RESUME)
			processCloudletTransmissionResume(cloudletId, userId, vmId);

	}

	/**
	 * Verifies if some cloudlet inside this Datacenter already finished to be
	 * transmitted. If yes, send it to the User/Broker
	 * 
	 * @pre $none
	 * @post $none
	 */
	// OK
	protected void checkCloudletTransmissionCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (((PSCloudletSchedulerSpaceShared) vm.getCloudletScheduler()).isTransmittedCloudlets()) {
					Cloudlet cl = ((PSCloudletSchedulerSpaceShared) vm.getCloudletScheduler())
							.getNextTransmittedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), PSCloudSimTags.CLOUDLET_TRANSMITTED, cl);
					}
				}
			}
		}
	}

	/**
	 * Updates transmission of each cloudlet running in this Datacenter. It is
	 * necessary because Hosts and VirtualMachines are simple objects, not
	 * entities. So, they don't receive events and updating cloudlets inside
	 * them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	// OK
	protected void updateCloudletTransmission() {
		// if some time passed since last processing
		// R: for term is to allow loop at simulation start. Otherwise, one
		// initial
		// simulation step is skipped and schedulers are not properly
		// initialized
		double clock = CloudSim.clock();
		double lastTranTime = getLastTransmissionTime();
		double minTimeBetweenEvents = CloudSim.getMinTimeBetweenEvents();
		if (clock < 0.111 || clock > lastTranTime + minTimeBetweenEvents) {
			List<? extends Host> list = getVmAllocationPolicy().getHostList();
			double smallerTime = Double.MAX_VALUE;
			// for each host...
			for (int i = 0; i < list.size(); i++) {
				PSHost host = (PSHost) list.get(i);
				// inform VMs to update processing
				double time = host.updateVmsTransmission(CloudSim.clock());
				// what time do we expect that the next cloudlet will finish?
				if (time < smallerTime) {
					smallerTime = time;
				}
			}
			// gurantees a minimal interval before scheduling the event
			if (smallerTime < CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.1E-6) 
				smallerTime = CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.1E-6;
			if (smallerTime != Double.MAX_VALUE) 
				send(getId(), (smallerTime - CloudSim.clock()), PSCloudSimTags.VM_DATACENTER_EVENT_TRANSMISSION);
			
			setLastTransmissionTime(CloudSim.clock());
		}
	}

	/**
	 * Processes a Cloudlet pause request.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet to be paused
	 * @param userId
	 *            ID of the cloudlet's owner
	 * @param ack
	 *            indicates if the event's sender expects to receive an
	 *            acknowledge message when the event finishes to be processed
	 * @param vmId
	 *            the id of the VM where the cloudlet has to be paused
	 * 
	 * @pre $none
	 * @post $none
	 */
	// OK
	protected void processCloudletTransmissionPause(int cloudletId, int userId, int vmId) {
		((PSCloudletSchedulerSpaceShared) getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId, userId)
				.getCloudletScheduler()).cloudletTransmissionPause(cloudletId);
	}

	/**
	 * Processes a Cloudlet resume request.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet to be resumed
	 * @param userId
	 *            ID of the cloudlet's owner
	 * @param ack
	 *            indicates if the event's sender expects to receive an
	 *            acknowledge message when the event finishes to be processed
	 * @param vmId
	 *            the id of the VM where the cloudlet has to be resumed
	 * 
	 * @pre $none
	 * @post $none
	 */
	// OK
	protected void processCloudletTransmissionResume(int cloudletId, int userId, int vmId) {
		double eventTime = ((PSCloudletSchedulerSpaceShared) getVmAllocationPolicy().getHost(vmId, userId)
				.getVm(vmId, userId).getCloudletScheduler()).cloudletTransmissionResume(cloudletId);

		if (eventTime > 0.0 && eventTime > CloudSim.clock())
			schedule(getId(), eventTime, PSCloudSimTags.VM_DATACENTER_EVENT_TRANSMISSION);
	}

}
