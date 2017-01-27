package net.amygdalum.patternsearchalgorithms.nfa;

public abstract class OrdinaryTransition extends AbstractTransition {

	public OrdinaryTransition(State origin, State target) {
		super(origin, target);
	}
	
	public abstract byte getFrom();
	
	public abstract byte getTo();

	public boolean accepts(byte b) {
		return b >= getFrom() && b <= getTo();
	}

}
