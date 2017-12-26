package br.ufg.inf.mcloudsim.core;

/**
 * Represents an event entity, which can be a publisher or a subscriber.
 * 
 * @author Raphael Gomes
 *
 */
public abstract class EdgeEventEntity extends EventEntity {

	/** Topic of entity events **/
	protected String topic;

	public EdgeEventEntity(String id, String topic) {
		super(id);
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeEventEntity other = (EdgeEventEntity) obj;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + this.topic + ")";
	}
}
