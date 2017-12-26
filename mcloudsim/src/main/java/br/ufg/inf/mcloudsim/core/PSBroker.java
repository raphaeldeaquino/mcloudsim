package br.ufg.inf.mcloudsim.core;

public class PSBroker extends CoreEventEntity {

	public PSBroker(String id) {
		super(id);

	}

	@Override
	public void setId(String id) {
		if (id == null || id.isEmpty() || !id.startsWith("B"))
			throw new IllegalArgumentException("Invalid broker ID: " + id);

		this.id = id;
	}

}
