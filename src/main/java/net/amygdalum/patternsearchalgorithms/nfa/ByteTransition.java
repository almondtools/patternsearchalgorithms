package net.amygdalum.patternsearchalgorithms.nfa;

public class ByteTransition extends OrdinaryTransition {

	private byte event;

	public ByteTransition(State origin, byte event, State target) {
		super(origin, target);
		this.event = event;
	}

	@Override
	public byte getFrom() {
		return event;
	}

	@Override
	public byte getTo() {
		return event;
	}

	@Override
	public Transition asPrototype() {
		return new ByteTransition(null, event, null);
	}

	@Override
	public String toString() {
		return "-{" + event + "}-> " + getTarget().getId();
	}

}
