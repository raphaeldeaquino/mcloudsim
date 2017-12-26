package br.ufg.inf.mcloudsim.simulator;

public class XmlParserException extends Exception {

	private static final long serialVersionUID = -2709942039695288745L;
	
	public XmlParserException(String msg) {
		super(msg);
	}
	
	public XmlParserException(String msg, Throwable e) {
		super(msg, e);
	}

}
