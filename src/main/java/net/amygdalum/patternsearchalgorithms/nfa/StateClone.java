package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;

import net.amygdalum.util.tuples.Pair;
import net.amygdalum.util.worklist.WorkSet;

public class StateClone {

	private State start;
	private IdentityHashMap<State, State> states;

	public StateClone(State start) {
		this.start = start;
		this.states = new IdentityHashMap<State, State>();
	}

	public static StateClone cloneTree(State start) {
		StateClone stateClone = new StateClone(start);
		
		stateClone.process();

		return stateClone;
	}

	private void process() {
		Queue<Pair<State, State>> workset = new WorkSet<>();
		State clonedStart = start.asPrototype();
		workset.add(new Pair<>(start, clonedStart));
		states.put(start, clonedStart);
		while (!workset.isEmpty()) {
			Pair<State, State> current = workset.remove();
			State state = current.left;
			State cloned = current.right;

			List<Pair<State, State>> next = transferState(state, cloned);
			workset.addAll(next);
		}
	}

	private List<Pair<State, State>> transferState(State state, State cloned) {
		List<Pair<State, State>> next = new ArrayList<>();

		if (state.isAccepting()) {
			cloned.setAccepting();
		}
		if (state.isSilent()) {
			cloned.setSilent();
		}
		for (Transition transition : state.transitions()) {
			State target = transition.getTarget();
			State clonedtarget = states.get(target);
			if (clonedtarget == null) {
				clonedtarget = target.asPrototype();
				states.put(target, clonedtarget);
				next.add(new Pair<>(target, clonedtarget));
			}
			Transition clonedtransition = transition.asPrototype().withOrigin(cloned).withTarget(clonedtarget);
			cloned.addTransition(clonedtransition);
		}
		return next;
	}

	public State get(State state) {
		return states.get(state);
	}

	public State getStart() {
		return states.get(start);
	}
}
