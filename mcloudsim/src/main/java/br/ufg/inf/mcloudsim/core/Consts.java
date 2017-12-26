package br.ufg.inf.mcloudsim.core;

public interface Consts {

	/**
	 * This value is chosen to be the ideal CPI value. See: J. Jeffers and J.
	 * Reinders. Intel Xeon Phi Coprocessor High- Performance Programming.
	 * Morgan Kaufmann, 2013
	 */
	public static final double INSTRUCTIONS_PER_CICLE = 4.0;
	
	public static final double GHZ = 10.0e9;
	
	/** One million. */
    public static final double MILLION = 10.0e6;
}
