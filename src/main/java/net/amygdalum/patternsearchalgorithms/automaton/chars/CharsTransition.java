package net.amygdalum.patternsearchalgorithms.automaton.chars;

public class CharsTransition extends OrdinaryTransition {

	private char from;
	private char to;

	public CharsTransition(State origin, char from, char to, State target) {
		super(origin, target);
		this.from = from;
		this.to = to;
	}

	@Override
	public char getFrom() {
		return from;
	}

	@Override
	public char getTo() {
		return to;
	}

	@Override
	public Transition asPrototype() {
		return new CharsTransition(null, from, to, null).withAction(getAction());
	}

	@Override
	public String toString() {
		return "-{" + from + ',' + to + "}-> " + getTarget().getId();
	}

}
