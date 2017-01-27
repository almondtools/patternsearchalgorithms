package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.builders.HashSets;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.worklist.WorkSet;

public class NFAMatcher {

	private ByteProvider bytes;
	private Set<Record> activeStates;

	public NFAMatcher(NFA automaton, ByteProvider bytes) {
		this.bytes = bytes;
		activeStates = epsilonClosure(automaton.getStart(), new Groups());
	}

	public boolean matches() {
		while (!bytes.finished()) {
			byte b = bytes.next();
			Set<Record> nextStates = new HashSet<>();
			for (Record activeState : activeStates) {
				State state = activeState.state;
				Groups groups = activeState.groups;
				List<OrdinaryTransition> transitions = state.nexts(b);
				for (OrdinaryTransition transition : transitions) {
					State target = transition.getTarget();
					Groups targetGroups = transition.executeAction(groups);
					nextStates.addAll(epsilonClosure(target, targetGroups));
				}
			}
			activeStates = nextStates;
		}
		for (Record activeState: activeStates) {
			State state = activeState.state;
			if (state.isAccepting()) {
				return true;
			}
		}
		return false;
	}

	private Set<Record> epsilonClosure(State state, Groups groups) {
		Set<Record> epsilons = HashSets.of(new Record(state, groups));
		Queue<EpsilonTransition> todo = new WorkSet<>();
		todo.addAll(state.epsilons());
		while (!todo.isEmpty()) {
			EpsilonTransition epsilon = todo.remove();
			State target = epsilon.getTarget();
			Groups targetGroups = epsilon.executeAction(groups);
			epsilons.add(new Record(target, targetGroups));
			todo.addAll(target.epsilons());
		}
		return epsilons;
	}

	private static class Record {
		public State state;
		public Groups groups;

		public Record(State state, Groups groups) {
			this.state = state;
			this.groups = groups;
		}
	}

}
