package net.amygdalum.patternsearchalgorithms.automaton.chars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.amygdalum.util.text.CharRange;

public class DFA {

	public int start;
	public int accepting; // accepting to infinity is accepting
	public int silent; // 0 to silent is silent
	public CharClassMapper mapper;
	public int[] transitions;

	public DFA(int start, int accepting, int silent, CharClassMapper mapper, int[] transitions) {
		this.start = start;
		this.accepting = accepting;
		this.silent = silent;
		this.mapper = mapper;
		this.transitions = transitions;
	}

	public static DFA from(NFA nfa) {
		nfa = nfa.clone();
		nfa.determinize();
		State start = nfa.getStart();
		State[] states = nfa.states();
		List<CharRange> ranges = nfa.getCharRanges();
		return new DFABuilder(ranges, start, states).build();
	}

	public int next(int s, char c) {
		return transitions[s * mapper.indexCount() + mapper.getIndex(c)];
	}

	public boolean accept(int s) {
		return s >= accepting;
	}

	public boolean silent(int s) {
		return s <= silent;
	}

	private static class DFABuilder implements Comparator<State> {

		private List<CharRange> liveRanges;
		private State start;
		private State[] states;
		private CharClassMapper mapper;
		private int[] transitions;
		private int accepting; // accepting to infinity is accepting
		private int silent; // 0 to silent is silent

		public DFABuilder(List<CharRange> ranges, State start, State[] states) {
			this.liveRanges = live(ranges, states);
			this.start = start;
			this.states = states;
		}

		private List<CharRange> live(List<CharRange> ranges, State[] states) {
			List<CharRange> live = new ArrayList<>();
			for (CharRange range : ranges) {
				char c = range.from;
				for (State state : states) {
					for (Transition transition : state.out()) {
						if (transition instanceof OrdinaryTransition && ((OrdinaryTransition) transition).accepts(c)) {
							live.add(range);
						}
					}
				}
			}
			return live;
		}

		private void computeTransitions() {
			int[] transitions = new int[states.length * mapper.indexCount()];
			for (int i = 0; i < states.length; i++) {
				nextchar: for (int index = 0; index < mapper.indexCount(); index++) {
					char c = mapper.representative(index);
					for (Transition next : states[i].out()) {
						if (next instanceof OrdinaryTransition && ((OrdinaryTransition) next).accepts(c)) {
							transitions[i * mapper.indexCount() + index] = next.getTarget().getId();
							continue nextchar;
						}
					}
					transitions[i * mapper.indexCount() + index] = -1;
				}
			}
			this.transitions = transitions;
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

		private void computeMapper() {
			boolean lowByte = computeLowByte();
			boolean smallRange = computeSmallRange(lowByte);
			if (smallRange) {
				this.mapper = new SmallRangeCharClassMapper(liveRanges);
			} else if (lowByte) {
				this.mapper = new LowByteCharClassMapper(liveRanges);
			} else {
				this.mapper = new BitMaskCharClassMapper(liveRanges);
			}
		}

		public boolean computeLowByte() {
			Set<Integer> highbytes = new HashSet<>();
			for (CharRange range : liveRanges) {
				highbytes.add(range.from & 0xff00);
				highbytes.add(range.to & 0xff00);
			}
			return highbytes.size() <= 1;
		}

		public boolean computeSmallRange(boolean lowByte) {
			if (liveRanges.isEmpty()) {
				return true;
			} else {
				char min = liveRanges.get(0).from;
				char max = liveRanges.get(liveRanges.size() - 1).to;
				if (lowByte) {
					return max - min <= 64;
				} else {
					return max - min <= 256;
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
			computeMapper();
			computeTransitions();

			return new DFA(start.getId(), accepting, silent, mapper, transitions);
		}

	}
}
