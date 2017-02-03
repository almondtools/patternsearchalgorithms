package net.amygdalum.patternsearchalgorithms.dfa;

import java.util.List;

import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.OrdinaryTransition;
import net.amygdalum.patternsearchalgorithms.nfa.State;

public class DFA {

	public int start;
	public boolean[] accept;
	public int[] transitions;

	public DFA(int start, boolean[] accept, int[] transitions) {
		this.start = start;
		this.accept = accept;
		this.transitions = transitions;
	}

	public static DFA from(NFA nfa) {
		nfa = nfa.clone();
		nfa.determinize();
		int start = nfa.getStart().getId();
		State[] states = nfa.states();
		boolean[] accept = accept(states);
		int[] transitions = transitions(states);
		return new DFA(start, accept, transitions);
	}

	private static boolean[] accept(State[] states) {
		boolean[] accept = new boolean[states.length];
		for (int i = 0; i < accept.length; i++) {
			accept[i] = states[i].isAccepting();
		}
		return accept;
	}

	private static int[] transitions(State[] states) {
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

	public int next(int s, byte b) {
		return transitions[s * 256 + (b & 0xff)];
	}

	public boolean accept(int s) {
		if (s < 0) {
			return false;
		} else {
			return accept[s];
		}
	}

}
