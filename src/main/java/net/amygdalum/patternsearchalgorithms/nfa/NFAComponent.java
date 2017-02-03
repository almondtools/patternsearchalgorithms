package net.amygdalum.patternsearchalgorithms.nfa;

import java.nio.charset.Charset;

import net.amygdalum.util.worklist.WorkSet;

public class NFAComponent implements Cloneable {

	public State start;
	public State end;

	public NFAComponent(State start, State end) {
		this.start = start;
		this.end = end;
	}

	public NFA toFullNFA(Charset charset) {
		if (end != null) {
			end.accept();
		}
		return new NFA(start, charset);
	}

	public NFAComponent reverse() {
		WorkSet<Transition> todo = new WorkSet<>();
		todo.addAll(start.transitions());
		while (!todo.isEmpty()) {
			Transition current = todo.remove();
			State target = current.getTarget();
			todo.addAll(target.transitions());
		}
		
		for (Transition transition : todo.getDone()) {
			State origin = transition.getOrigin();
			State target = transition.getTarget();
			Action action = transition.getAction();
			
			origin.removeTransition(transition);
			target.addTransition(transition.asPrototype().withOrigin(target).withTarget(origin).withAction(action));
		}
		
		return new NFAComponent(end, start);
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