package net.amygdalum.patternsearchalgorithms.nfa;

public abstract class AbstractTransition implements Transition {

	private Action action;
	private State origin;
	private State target;

	public AbstractTransition(State origin, State target) {
		this.origin = origin;
		this.target = target;
	}

	@Override
	public Transition withOrigin(State origin) {
		this.origin = origin;
		return this;
	}
	
	@Override
	public Transition withTarget(State target) {
		this.target = target;
		return this;
	}

	@Override
	public Transition withAction(Action action) {
		this.action = action;
		return this;
	}
	
	@Override
	public State getOrigin() {
		return origin;
	}

	@Override
	public State getTarget() {
		return target;
	}
	
	@Override
	public Groups executeAction(Groups groups) {
		if (action == null) {
			return groups;
		} else {
			return action.applyTo(groups);
		}
	}
}
