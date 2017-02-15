package net.amygdalum.patternsearchalgorithms.automaton.bytes;

public abstract class OrdinaryTransition extends AbstractTransition {

	public OrdinaryTransition(State origin, State target) {
		super(origin, target);
	}
	
	public abstract byte getFrom();
	
	public abstract byte getTo();

	public boolean accepts(byte b) {
		int i = b & 0xff;
		return i >= (getFrom() & 0xff) && i <= (getTo() & 0xff);
	}

}
