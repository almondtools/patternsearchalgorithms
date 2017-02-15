package net.amygdalum.patternsearchalgorithms.automaton.bytes;

public class BytesTransition extends OrdinaryTransition {

	private byte from;
	private byte to;

	public BytesTransition(State origin, byte from, byte to, State target) {
		super(origin, target);
		this.from = from;
		this.to = to;
	}

	@Override
	public byte getFrom() {
		return from;
	}

	@Override
	public byte getTo() {
		return to;
	}

	@Override
	public Transition asPrototype() {
		return new BytesTransition(null, from, to, null).withAction(getAction());
	}

	@Override
	public String toString() {
		return "-{" + from + ',' + to + "}-> " + getTarget().getId();
	}

}
