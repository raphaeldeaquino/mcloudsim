/*
 * Title:        Mobile CloudSim Toolkit
 * Description:  Extension of CloudSim Toolkit for Modeling and Simulation of Publish/Subscribe 
 * 				 Communication Paradigm with Subscriber Connectivity Change
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2014-2016, Universidade Federal de Goiás, Brazil
 */

package br.ufg.inf.mcloudsim.simulator;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * A cloudlet type for representing pub/sub interaction. <BR>
 * An PSCloudlet can be of type PUBLISH or SUBSCRIBE. It inherits
 * {@link Cloudlet} with this information and the arrival time of cloudlet. <BR>
 * The information used to compare two cloudlets is the identifier.
 * 
 * @author Raphael Gomes
 *
 */
// OK
public class PSCloudlet extends Cloudlet implements Comparable<PSCloudlet> {

	public enum cloudletType {
		IN, OUT
	}

	private cloudletType type;
	private long bytes;
	private double arrivalTime;
	private String pathId;
	private String brokerId;
	private int transmissionIndex;
	private final List<TransmissionResource> transmissionResList;

	public PSCloudlet(int cloudletId, String pathId, String brokerId, long cloudletLength, long bytes, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, cloudletType type,
			double arrivalTime) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		this.bytes = Math.max(1, bytes);
		this.pathId = pathId;
		this.brokerId = brokerId;
		this.type = type;
		this.arrivalTime = arrivalTime;
		this.transmissionIndex = -1;
		// Normally, a Cloudlet is only executed on a resource without being
		// migrated to others. Hence, to reduce memory consumption, set the
		// size of this ArrayList to be less than the default one.
		transmissionResList = new ArrayList<TransmissionResource>(2);
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public String getPathId() {
		return pathId;
	}

	public void setPathId(String pathId) {
		this.pathId = pathId;
	}

	public String getBrokerId() {
		return brokerId;
	}

	public void setBrokerId(String brokerId) {
		this.brokerId = brokerId;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public cloudletType getType() {
		return type;
	}
	
	@Override
	public void setResourceParameter(final int resourceID, final double cost) {
		final TransmissionResource res = new TransmissionResource();
        res.resourceId = resourceID;
        res.costPerSec = cost;
        res.resourceName = CloudSim.getEntityName(resourceID);
        
     // add into a list if moving to a new grid resource
       transmissionResList.add(res);
		
		transmissionIndex++;
		super.setResourceParameter(resourceID, cost);
	}

	public boolean isInput() {
		return this.type == cloudletType.IN;
	}

	public boolean isOutput() {
		return this.type == cloudletType.OUT;
	}

	/**
	 * Checks whether this Cloudlet has finished execution or not.
	 *
	 * @return <tt>true</tt> if this Cloudlet has finished execution,
	 *         <tt>false</tt> otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean isTransmitted() {
		if (transmissionIndex == -1) {
			return false;
		}

		boolean completed = false;

		// if result is 0 or -ve then this Cloudlet has finished
		final long finish = transmissionResList.get(transmissionIndex).finishedSoFar;
		final long result = bytes - finish;
		if (result <= 0.0) {
			completed = true;
		}
		return completed;
	}
	
	public void setCloudletTransmittedSoFar(final long length) {
        // if length is -ve then ignore
        if (length < 0.0 || transmissionIndex < 0) {
            return;
        }

        final TransmissionResource res = transmissionResList.get(transmissionIndex);
        res.finishedSoFar = length;
    }
	
	public void setTransmissionStartTime(final double clockTime) {
        if (clockTime < 0.0 || transmissionIndex < 0) {
            return;
        }

        final TransmissionResource res = transmissionResList.get(transmissionIndex);
        res.submissionTime = clockTime;
    }
	
	public double getTransmissionStartTime() {
        if (transmissionIndex == -1) {
            return 0.0;
        }
        return transmissionResList.get(transmissionIndex).submissionTime;
    }
	
	public void setTransParam(final double wallTime, final double actualTime) {
        if (wallTime < 0.0 || actualTime < 0.0 || transmissionIndex < 0) {
            return;
        }

        final TransmissionResource res = transmissionResList.get(transmissionIndex);
        res.wallClockTime = wallTime;
        res.actualCPUTime = actualTime;
    }
	
	public long getCloudletTransmittedSoFar(final int resId) {
        TransmissionResource resource = getTransmissionResourceById(resId);
        if (resource != null) {
            return resource.finishedSoFar;
        }
        return 0;
    }
	
	public long getCloudletTransmittedSoFar() {
        if (transmissionIndex == -1) {
            return bytes;
        }

        final long finish = transmissionResList.get(transmissionIndex).finishedSoFar;
        if (finish > bytes) {
            return bytes;
        }

        return finish;
    }
	
	public TransmissionResource getTransmissionResourceById(final int resourceId) {
        for (TransmissionResource resource : transmissionResList) {
            if (resource.resourceId == resourceId) {
                return resource;
            }
        }
        return null;
    }

	// ////////////////////// INTERNAL CLASS ///////////////////////////////////
	/**
	 * Internal class that keeps track of Cloudlet's movement in different
	 * CloudResources. Each time a cloudlet is run on a given VM, the cloudlet's
	 * execution history on each VM is registered at {@link Cloudlet#resList}
	 */
	private static class TransmissionResource {

		/**
		 * Cloudlet's submission (arrival) time to a CloudResource.
		 */
		public double submissionTime = 0.0;

		/**
		 * The time this Cloudlet resides in a CloudResource (from arrival time
		 * until departure time, that may include waiting time).
		 */
		public double wallClockTime = 0.0;

		/**
		 * The total time the Cloudlet spent being executed in a CloudResource.
		 */
		public double actualCPUTime = 0.0;

		/**
		 * Cost per second a CloudResource charge to execute this Cloudlet.
		 */
		public double costPerSec = 0.0;

		/**
		 * Cloudlet's length finished so far.
		 */
		public long finishedSoFar = 0;

		/**
		 * a CloudResource id.
		 */
		public int resourceId = -1;

		/**
		 * a CloudResource name.
		 */
		public String resourceName = null;

	}

	// ////////////////////// End of Internal Class //////////////////////////

	public int compareTo(PSCloudlet o) {
		return this.getCloudletId() - o.getCloudletId();
	}

	@Override
	public String toString() {
		return type.toString().substring(0, 1) + "" + getCloudletId() + " [" + getExecStartTime() + ":"
				+ getFinishTime() + ":" + getActualCPUTime() + "]";
	}

}
