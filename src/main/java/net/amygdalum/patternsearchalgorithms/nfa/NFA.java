package net.amygdalum.patternsearchalgorithms.nfa;

import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.worklist.WorkSet;

public class NFA {

	private State start;
	private Charset charset;

	public NFA(State start, Charset charset) {
		this.start = start;
		this.charset = charset;
	}

	public State getStart() {
		return start;
	}

	public Charset getCharset() {
		return charset;
	}

	public Set<State> states() {
		WorkSet<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State current = todo.remove();
			todo.addAll(current.reachableStates());
		}
		return todo.getDone();
	}

	public void prune() {
		eliminateEpsilons();
		//remove duplicate final states
		//merge adjacent transitions
	}

	public void determinize() {
		eliminateEpsilons();
		//remove duplicate final states
		//merge adjacent transitions
		//split overlapping transitions
		//determinize
		//totalize
		//cleanup
		//minimize
	}

	private void eliminateEpsilons() {
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (State target : state.reachableStates()) {
				todo.add(target);
			}
			for (EpsilonTransition epsilon : transitiveEpsilons(state)) {
				State target = epsilon.getTarget();
				state.removeTransition(epsilon);
				if (target.isAccepting()) {
					state.accept();
				}
				for (Transition t : target.ordinaries()) {
					Transition inlined = t.asPrototype()
						.withOrigin(state)
						.withTarget(t.getTarget());
					state.addTransition(inlined);
				}
			}
		}
	}

	private Set<EpsilonTransition> transitiveEpsilons(State state) {
		Set<EpsilonTransition> epsilons = new LinkedHashSet<>();
		Queue<State> todo = new WorkSet<>();
		todo.add(state);
		while (!todo.isEmpty()) {
			State current = todo.remove();
			for (EpsilonTransition epsilon : current.epsilons()) {
				epsilons.add(epsilon);
				todo.add(epsilon.getTarget());
			}
		}
		return epsilons;
	}

}
