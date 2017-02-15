package net.amygdalum.patternsearchalgorithms.automaton.chars;

public class CharTransition extends OrdinaryTransition {

	private char event;

	public CharTransition(State origin, char event, State target) {
		super(origin, target);
		this.event = event;
	}

	@Override
	public char getFrom() {
		return event;
	}

	@Override
	public char getTo() {
		return event;
	}

	@Override
	public Transition asPrototype() {
		return new CharTransition(null, event, null).withAction(getAction());
	}

	@Override
	public String toString() {
		return "-{" + event + "}-> " + getTarget().getId();
	}

}
