package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import net.amygdalum.util.worklist.WorkSet;

public class NFAComponent implements Cloneable {

	public State start;
	public State end;

	public NFAComponent(State start, State end) {
		this.start = start;
		this.end = end;
	}

	public NFAComponent reverse() {
		WorkSet<Transition> todo = new WorkSet<>();
		todo.addAll(start.out());
		while (!todo.isEmpty()) {
			Transition current = todo.remove();
			State target = current.getTarget();
			todo.addAll(target.out());
		}
		
		for (Transition transition : todo.getDone()) {
			State origin = transition.getOrigin();
			State target = transition.getTarget();
			Action action = transition.getAction();
			
			transition.remove();
			transition.asPrototype().withOrigin(target).withTarget(origin).withAction(action).connect();
		}
		
		return new NFAComponent(end, start);
	}

	public NFAComponent silent() {
		WorkSet<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State current = todo.remove();
			current.setSilent();
		}		
		return this;
	}

	@Override
	protected NFAComponent clone() {
		try {
			NFAComponent clone = (NFAComponent) super.clone();
			StateClone statClone = StateClone.cloneTree(start);
			clone.start = statClone.get(start);
			clone.end = statClone.get(end);
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}