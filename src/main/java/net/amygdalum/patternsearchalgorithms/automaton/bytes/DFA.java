package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DFA {

	public int start;
	public int accepting; // accepting to infinity is accepting
	public int silent; // 0 to silent is silent
	public int[] transitions;

	public DFA(int start, int[] transitions, int accepting, int silent) {
		this.start = start;
		this.transitions = transitions;
		this.accepting = accepting;
		this.silent = silent;
	}

	public static DFA from(NFA nfa) {
		nfa = nfa.clone();
		nfa.determinize();
		State start = nfa.getStart();
		State[] states = nfa.states();
		return new DFABuilder(start, states).build();
	}

	public int next(int s, byte b) {
		return transitions[s * 256 + (b & 0xff)];
	}

	public boolean accept(int s) {
		return s >= accepting;
	}
	
	public boolean silent(int s) {
		return s <= silent;
	}

	private static class DFABuilder implements Comparator<State> {

		private State start;
		private State[] states;
		public int accepting; // accepting to infinity is accepting
		public int silent; // 0 to silent is silent

		public DFABuilder(State start, State[] states) {
			this.start = start;
			this.states = states;
		}

		private int[] transitions() {
			int[] transitions = new int[states.length * 256];
			for (int i = 0; i < states.length; i++) {
				for (int j = 0; j < 256; j++) {
					List<OrdinaryTransition> next = states[i].nexts((byte) j);
					if (next.size() == 1) {
						transitions[i * 256 + j] = next.get(0).getTarget().getId();
					} else {
						transitions[i * 256 + j] = -1;
					}
				}
			}
			return transitions;
		}

		private void partitionStates() {
			Arrays.sort(states, this);
			silent = -1;
			accepting = 0;
			for (int i = 0; i < states.length; i++) {
				states[i].setId(i);
				if (states[i].isSilent()) {
					silent = i;
				}
				if (!states[i].isAccepting()) {
					accepting = i + 1;
				}
			}
		}

		@Override
		public int compare(State s1, State s2) {
			boolean accept1 = s1.isAccepting();
			boolean accept2 = s2.isAccepting();
			int compare = Boolean.compare(accept1, accept2);
			if (compare == 0) {
				boolean silent1 = s1.isSilent();
				boolean silent2 = s2.isSilent();
				compare = Boolean.compare(silent2, silent1);
			}
			return compare;
		}

		public DFA build() {
			partitionStates();

			return new DFA(start.getId(), transitions(), accepting, silent);
		}

	}
}
