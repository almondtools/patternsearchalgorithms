package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class State implements Cloneable {

	private int id;
	private boolean accept;
	private List<Transition> transitions;

	public State() {
		id = System.identityHashCode(this);
		transitions = new ArrayList<>();
	}

	public State(int transitionCount) {
		id = System.identityHashCode(this);
		transitions = new ArrayList<>(transitionCount);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public void accept() {
		this.accept = true;
	}

	public void accept(boolean accept) {
		this.accept = accept;
	}

	public boolean isAccepting() {
		return accept;
	}

	public State asPrototype() {
		return new State(transitions.size());
	}

	public void addTransition(Transition transition) {
		transitions.add(transition);
	}

	public void removeTransition(Transition transition) {
		transitions.remove(transition);
	}

	public void updateTransitions(Collection<Transition> transitions) {
		this.transitions.clear();
		this.transitions.addAll(transitions);
	}

	public List<Transition> transitions() {
		return transitions;
	}

	public List<OrdinaryTransition> ordinaries() {
		List<OrdinaryTransition> next = new ArrayList<>();
		for (Transition transition : transitions) {
			if (transition instanceof OrdinaryTransition) {
				next.add((OrdinaryTransition) transition);
			}
		}
		return next;
	}

	public List<OrdinaryTransition> nexts(byte b) {
		List<OrdinaryTransition> next = new ArrayList<>();
		for (Transition transition : transitions) {
			if (transition instanceof OrdinaryTransition && ((OrdinaryTransition) transition).accepts(b)) {
				next.add((OrdinaryTransition) transition);
			}
		}
		return next;
	}

	public List<EpsilonTransition> epsilons() {
		List<EpsilonTransition> epsilons = new ArrayList<>();
		for (Transition transition : transitions) {
			if (transition instanceof EpsilonTransition) {
				epsilons.add((EpsilonTransition) transition);
			}
		}
		return epsilons;
	}

	public Set<State> reachableStates() {
		Set<State> states = new LinkedHashSet<>();
		for (Transition transition : transitions) {
			states.add(transition.getTarget());
		}
		return states;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getId()).append(" {\n");
		for (Transition transition : transitions) {
			buffer.append(transition.toString()).append('\n');
		}
		buffer.append('}');
		return buffer.toString();
	}

}
