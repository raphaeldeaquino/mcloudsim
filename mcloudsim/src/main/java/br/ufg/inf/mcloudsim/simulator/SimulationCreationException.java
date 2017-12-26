package br.ufg.inf.mcloudsim.simulator;

public class SimulationCreationException extends Exception {

	private static final long serialVersionUID = -8734900715162980813L;
	
	public SimulationCreationException(String msg) {
		super(msg);
	}

	public SimulationCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
