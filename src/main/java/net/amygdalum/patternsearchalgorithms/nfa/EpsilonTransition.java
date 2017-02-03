package net.amygdalum.patternsearchalgorithms.nfa;

public class EpsilonTransition extends AbstractTransition {

	public EpsilonTransition(State origin, State target) {
		super(origin, target);
	}

	@Override
	public Transition asPrototype() {
		return new EpsilonTransition(null, null).withAction(getAction());
	}
	
	@Override
	public String toString() {
		return "-> " + getTarget().getId();
	}

}
