package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.tuples.Pair;
import net.amygdalum.util.worklist.WorkSet;

public class State implements Cloneable {

	private boolean accept;
	private List<Transition> transitions;

	public State() {
		transitions = new ArrayList<>();
	}

	public State(int transitionCount) {
		transitions = new ArrayList<>(transitionCount);
	}

	public int getId() {
		return System.identityHashCode(this);
	}

	public void accept() {
		this.accept = true;
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

	public Map<State, State> cloneTree() {
		Map<State, State> states = new IdentityHashMap<State, State>();
		Queue<Pair<State, State>> workset = new WorkSet<>();
		workset.add(new Pair<>(this, this.asPrototype()));
		while (!workset.isEmpty()) {
			Pair<State, State> current = workset.remove();
			State state = current.left;
			State cloned = current.right;
			states.put(state, cloned);

			for (Transition transition : state.transitions) {
				State target = transition.getTarget();
				State clonedtarget = states.get(target);
				if (clonedtarget == null) {
					clonedtarget = target.asPrototype();
					workset.add(new Pair<>(target, clonedtarget));
				}
				Transition clonedtransition = transition.asPrototype().withOrigin(cloned).withTarget(clonedtarget);
				cloned.addTransition(clonedtransition);
			}
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
