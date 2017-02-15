package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.bits.BitSet;
import net.amygdalum.util.builders.ArrayLists;
import net.amygdalum.util.map.BitSetObjectMap;
import net.amygdalum.util.text.CharRange;
import net.amygdalum.util.worklist.WorkSet;

public class NFA implements Cloneable {

	private State start;
	private List<CharRange> charRanges;
	private State[] states;
	private List<Transition>[] reachingTransitions;

	public NFA(State start) {
		init(start);
	}

	private void init(State start) {
		this.start = start;
		this.charRanges = computeEquivalentCharRanges(start);
		this.states = enumerateStates(start);
		this.reachingTransitions = reachingTransitions(states);
		markLive(states, reachingTransitions);
	}

	public State getStart() {
		return start;
	}

	public State[] states() {
		return states;
	}
	
	public List<CharRange> getCharRanges() {
		return charRanges;
	}

	private static void markLive(State[] states, List<Transition>[] reachingTransitions) {
		WorkSet<State> todo = new WorkSet<>();
		for (State state : states) {
			if (state.isAccepting()) {
				todo.add(state);
			}
		}
		while (!todo.isEmpty()) {
			State current = todo.remove();
			current.setLive();
			List<Transition> nexts = reachingTransitions[current.getId()];
			for (Transition next : nexts) {
				todo.add(next.getOrigin());
			}
		}
	}

	public void prune() {
		eliminateEpsilons(true);
		mergeAdjacentTransitions();
		eliminateDeadStates();
	}

	public void determinize() {
		eliminateEpsilons(false);
		mergeAdjacentTransitions();
		eliminateDeadStates();
		determinizeStates();
		eliminateDeadStates();
		minimizeStates();
	}

	public Object tabled() {
		determinize();
		return null;
	}

	private void minimizeStates() {

		//P := {F, Q \ F};
		List<BitSet> partitions = new LinkedList<>();
		BitSet accepting = BitSet.empty(states.length);
		BitSet notaccepting = BitSet.empty(states.length);
		for (int i = 0; i < states.length; i++) {
			if (states[i].isAccepting()) {
				accepting.set(i);
			} else {
				notaccepting.set(i);
			}
		}
		if (!accepting.isEmpty()) {
			partitions.add(accepting);
		}
		if (!notaccepting.isEmpty()) {
			partitions.add(notaccepting);
		}

		Queue<BitSet> todo = new LinkedList<>();
		todo.add(accepting);
		// W := {F};

		// while (W is not empty) do
		while (!todo.isEmpty()) {
			// choose and remove a set A from W
			BitSet current = todo.remove();
			// for each c in Σ do
			for (CharRange range : charRanges) {
				char representative = range.from;
				// let X be the set of states for which a transition on c leads to a state in A
				BitSet reachingStates = reachingStates(current, representative);
				// for each set Y in P for which X ∩ Y is nonempty and Y \ X is nonempty do
				if (reachingStates.isEmpty()) {
					continue;
				}
				for (int i = 0; i < partitions.size(); i++) {
					BitSet partition = partitions.get(i);
					BitSet intersection = partition.and(reachingStates);
					BitSet remainder = partition.andNot(reachingStates);
					if (intersection.isEmpty() || remainder.isEmpty()) {
						continue;
					}
					List<BitSet> newpart = asList(intersection, remainder);
					// replace Y in P by the two sets X ∩ Y and Y \ X
					partitions.remove(i);
					partitions.addAll(i, newpart);
					i++;
					// if Y is in W
					if (todo.contains(partition)) {
						// replace Y in W by the same two sets
						todo.remove(partition);
						todo.addAll(newpart);
					} else {
						// if |X ∩ Y| <= |Y \ X|
						if (intersection.bitCount() <= remainder.bitCount()) {
							// add X ∩ Y to W
							todo.add(intersection);
						} else {
							// add Y \ X to W
							todo.add(remainder);
						}
					}
				}
			}
		}

		State newstart = mergeStates(partitions);
		init(newstart);
	}

	private State mergeStates(List<BitSet> partitions) {
		Map<State, State> mapping = new IdentityHashMap<>();
		for (BitSet bits : partitions) {
			int pos = bits.nextSetBit(0);
			State representative = states[pos];
			while (pos > -1) {
				State state = states[pos];
				if (state.isAccepting()) {
					representative.setAccepting();
				}
				if (!state.isSilent()) {
					representative.setSilent(false);
				}
				mapping.put(state, representative);
				pos = bits.nextSetBit(pos + 1);
			}
		}

		State newstart = mapping.get(start);
		Queue<State> toMap = new WorkSet<>();
		toMap.add(newstart);
		while (!toMap.isEmpty()) {
			State current = toMap.remove();
			for (Transition transition : current.transitions()) {
				State target = transition.getTarget();
				State mappedTarget = mapping.get(target);
				if (target != mappedTarget) {
					target = mappedTarget;
					transition.withTarget(mappedTarget);
				}
				toMap.add(target);
			}
		}
		return newstart;
	}

	private BitSet reachingStates(BitSet bits, char event) {
		BitSet reachingStates = BitSet.empty(states.length);

		int pos = bits.nextSetBit(0);
		while (pos > -1) {
			State state = states[pos];
			List<Transition> reaching = reachingTransitions[state.getId()];
			if (reaching != null) {
				for (Transition transition : reaching) {
					if (transition instanceof OrdinaryTransition && ((OrdinaryTransition) transition).accepts(event)) {
						State origin = transition.getOrigin();
						reachingStates.set(origin.getId());
					}
				}
			}
			pos = bits.nextSetBit(pos + 1);
		}
		return reachingStates;
	}

	private void determinizeStates() {
		BitSetObjectMap<State> dStates = new BitSetObjectMap<State>(null);
		Queue<BitSet> todo = new WorkSet<>();

		BitSet startbits = BitSet.bits(states.length, start.getId());
		todo.add(startbits);
		State dStart = new State();
		dStates.add(startbits, dStart);

		while (!todo.isEmpty()) {
			BitSet current = todo.remove();
			State dState = dStates.get(current);
			transferAccept(current, dState);

			for (CharRange range : charRanges) {
				char from = range.from;
				char to = range.to;
				BitSet next = next(current, from);
				State target = dStates.get(next);
				if (target == null) {
					target = new State();
					dStates.add(next, target);
				}
				if (from == to) {
					dState.addTransition(new CharTransition(dState, from, target));
				} else {
					dState.addTransition(new CharsTransition(dState, from, to, target));
				}
				todo.add(next);
			}
		}
		init(dStart);
	}

	private void transferAccept(BitSet bits, State dState) {
		boolean accepting = false;
		boolean silent = true;
		int pos = bits.nextSetBit(0);
		while (pos > -1 && (silent || !accepting)) {
			State state = states[pos];
			accepting |= state.isAccepting();
			silent &= state.isSilent();
			pos = bits.nextSetBit(pos + 1);
		}
		dState.setAccepting(accepting);
		dState.setSilent(silent);
	}

	private BitSet next(BitSet bits, char value) {
		BitSet result = BitSet.empty(states.length);
		int pos = bits.nextSetBit(0);
		while (pos > -1) {
			State state = states[pos];
			for (OrdinaryTransition transition : state.nexts(value)) {
				result.set(transition.getTarget().getId());
			}
			pos = bits.nextSetBit(pos + 1);
		}
		return result;
	}

	private static State[] enumerateStates(State start) {
		List<State> states = new ArrayList<>();
		WorkSet<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State current = todo.remove();
			current.setId(states.size());
			states.add(current);
			todo.addAll(current.reachableStates());
		}
		return states.toArray(new State[0]);
	}

	private static List<CharRange> computeEquivalentCharRanges(State start) {
		CharRangeAccumulator acc = new CharRangeAccumulator();

		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (OrdinaryTransition transition : state.ordinaries()) {
				acc.split(transition.getFrom(), transition.getTo());
				todo.add(transition.getTarget());
			}
		}
		return acc.getRanges();
	}

	@SuppressWarnings("unchecked")
	private static List<Transition>[] reachingTransitions(State[] states) {
		List<Transition>[] reaching = new List[states.length];
		for (int i = 0; i < reaching.length; i++) {
			reaching[i] = new ArrayList<>();
		}

		for (State state : states) {
			for (Transition transition : state.transitions()) {
				State target = transition.getTarget();
				reaching[target.getId()].add(transition);
			}
		}
		return reaching;
	}

	private void mergeAdjacentTransitions() {
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			List<Transition> sortedTransitions = new ArrayList<>(state.transitions());
			Collections.sort(sortedTransitions, new TransitionComparator());

			List<Transition> mergedTransitions = new ArrayList<>(sortedTransitions.size());

			Transition last = null;

			for (Transition transition : sortedTransitions) {
				if (last == null) {
					last = transition;
				} else {
					Transition join = tryJoin(last, transition);
					if (join != null) {
						last = join;
					} else {
						mergedTransitions.add(last);
						last = transition;
					}
				}
			}
			if (last != null) {
				mergedTransitions.add(last);
			}
			if (mergedTransitions.size() < sortedTransitions.size()) {
				state.updateTransitions(mergedTransitions);
			}
		}
	}

	private Transition tryJoin(Transition t1, Transition t2) {
		if (t1.getTarget() != t2.getTarget() || t1.getOrigin() != t2.getOrigin()) {
			return null;
		}
		State origin = t1.getOrigin();
		State target = t1.getTarget();

		if (t1 instanceof EpsilonTransition && t2 instanceof EpsilonTransition) {
			return new EpsilonTransition(origin, target);
		}
		if (t1 instanceof OrdinaryTransition && t2 instanceof OrdinaryTransition) {
			OrdinaryTransition ot1 = (OrdinaryTransition) t1;
			OrdinaryTransition ot2 = (OrdinaryTransition) t2;

			int from1 = ot1.getFrom() & 0xff;
			int to1 = ot1.getTo() & 0xff;
			int from2 = ot2.getFrom() & 0xff;
			int to2 = ot2.getTo() & 0xff;

			if (from2 >= from1 && from2 <= to1 + 1 || from1 >= from2 && from1 <= to2 + 1) {

				char from = (char) Math.min(from1, from2);
				char to = (char) Math.max(to1, to2);

				if (from == to) {
					return new CharTransition(origin, from, target);
				} else {
					return new CharsTransition(origin, from, to, target);
				}
			}
		}
		return null;
	}

	private void eliminateDeadStates() {
		
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State current = todo.remove();
			Iterator<Transition> transitionIterator = current.transitions().iterator();
			while (transitionIterator.hasNext()) {
				Transition transition = transitionIterator.next();
				State target = transition.getTarget();
				if (target.isLive()) {
					todo.add(target);
				} else {
					transitionIterator.remove();
				}
			}
		}
		init(start);
	}

	private void eliminateEpsilons(boolean preserveActions) {
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (State target : state.reachableStates()) {
				todo.add(target);
			}
			List<EpsilonTransition> epsilons = state.epsilons();
			for (EpsilonTransition epsilon : transitiveEpsilons(epsilons, preserveActions)) {
				State origin = epsilon.getOrigin();
				if (origin == state) {
					state.removeTransition(epsilon);
				}
				State target = epsilon.getTarget();
				if (target.isAccepting()) {
					state.setAccepting();
				}
				if (!target.isSilent()) {
					state.setSilent(false);
				}
				for (OrdinaryTransition t : target.ordinaries()) {
					Transition inlined = t.asPrototype()
						.withOrigin(state)
						.withTarget(t.getTarget());
					state.addTransition(inlined);
				}
				if (preserveActions) {
					for (EpsilonTransition t : target.epsilons()) {
						Action action = t.getAction();
						if (action != null) {
							Transition inlined = t.asPrototype()
								.withOrigin(state)
								.withTarget(t.getTarget())
								.withAction(action);
							state.addTransition(inlined);
						}
					}
				}
			}
		}
	}

	private Set<EpsilonTransition> transitiveEpsilons(Collection<EpsilonTransition> epsilons, boolean preserveActions) {
		WorkSet<EpsilonTransition> todo = new WorkSet<>();
		todo.addAll(epsilons);
		while (!todo.isEmpty()) {
			EpsilonTransition current = todo.remove();
			if (preserveActions && current.getAction() != null) {
				todo.remove(current);
				continue;
			}
			State target = current.getTarget();
			for (EpsilonTransition epsilon : target.epsilons()) {
				todo.add(epsilon);
			}
		}
		return todo.getDone();
	}

	public NFAComponent asComponent() {
		State end = new State();
		for (State state : states) {
			if (state.isAccepting()) {
			state.setAccepting(false);
			state.addTransition(new EpsilonTransition(state, end));
			}
		}
		return new NFAComponent(start, end);
	}

	@Override
	public NFA clone() {
		try {
			NFA nfa = (NFA) super.clone();
			StateClone stateClone = StateClone.cloneTree(start);
			nfa.init(stateClone.getStart());
			return nfa;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	private static class CharRangeAccumulator {

		private List<CharRange> ranges;

		public CharRangeAccumulator() {
			ranges = ArrayLists.of(new CharRange(MIN_VALUE, MAX_VALUE));
		}

		public List<CharRange> getRanges() {
			return ranges;
		}

		public void split(char from, char to) {
			for (int i = 0; i < ranges.size(); i++) {
				CharRange currentRange = ranges.get(i);
				if (currentRange.contains(from) && currentRange.contains(to)) {
					i = replace(i, currentRange.splitAround(from, to));
				} else if (currentRange.contains(from)) {
					i = replace(i, currentRange.splitBefore(from));
				} else if (currentRange.contains(to)) {
					i = replace(i, currentRange.splitAfter(to));
				}
			}
		}

		public int replace(int i, List<CharRange> replacement) {
			ranges.remove(i);
			ranges.addAll(i, replacement);
			return i + replacement.size() - 1;
		}

	}

}
