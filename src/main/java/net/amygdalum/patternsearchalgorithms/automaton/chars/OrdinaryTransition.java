package net.amygdalum.patternsearchalgorithms.automaton.chars;

public abstract class OrdinaryTransition extends AbstractTransition {

	public OrdinaryTransition(State origin, State target) {
		super(origin, target);
	}
	
	public abstract char getFrom();
	
	public abstract char getTo();

	public boolean accepts(char c) {
		return c >= getFrom() && c <= getTo();
	}

}
