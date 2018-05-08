package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.amygdalum.util.bits.BitSet;
import net.amygdalum.util.builders.ArrayLists;
import net.amygdalum.util.text.ByteRange;
import net.amygdalum.util.worklist.WorkSet;

public class NFA implements Cloneable {

	private Charset charset;
	private State start;
	private State error;
	private List<ByteRange> byteRanges;
	private State[] states;
	private List<Transition>[] reachingTransitions;

	public NFA(State start, Charset charset) {
		this.charset = charset;
		init(start);
	}

	private void init(State start) {
		this.start = start;
		this.byteRanges = computeEquivalentByteRanges(start);
		this.states = enumerateStates(start);
		this.reachingTransitions = reachingTransitions(states);
		clean();
	}

	private void init(State start, State error) {
		this.start = start;
		this.error = error;
		this.byteRanges = computeEquivalentByteRanges(start);
		this.states = enumerateStates(start);
		this.reachingTransitions = reachingTransitions(states);
		clean();
	}

	public State getStart() {
		return start;
	}

	public Charset getCharset() {
		return charset;
	}

	public State[] states() {
		return states;
	}

	public boolean isLive(State state) {
		return state == start || !reachingTransitions[state.getId()].isEmpty();
	}

	private void clean() {
		WorkSet<State> live = new WorkSet<>();
		Set<State> dead = new HashSet<>();
		for (State state : states) {
			if (state.isAccepting()) {
				live.add(state);
			} else {
				dead.add(state);
			}
		}
		while (!live.isEmpty()) {
			State current = live.remove();
			List<Transition> liveTransitions = reachingTransitions[current.getId()];
			for (Transition liveTransition : liveTransitions) {
				State nextLive = liveTransition.getOrigin();
				live.add(nextLive);
				dead.remove(nextLive);
			}
		}
		
		if (error != null) {
			dead.remove(error);
		}
		
		List<Transition> empty = emptyList();
		for (State current : dead) {
			current.updateTransitions(empty);
			List<Transition> deadTransitions = reachingTransitions[current.getId()];
			for (Transition deadTransition : deadTransitions) {
				State origin = deadTransition.getOrigin();
				origin.removeTransition(deadTransition);
			}
			reachingTransitions[current.getId()] = empty;
		}

	}

	public void prune() {
		eliminateEpsilons(true);
		mergeAdjacentTransitions();
	}

	public void determinize() {
		eliminateEpsilons(false);
		mergeAdjacentTransitions();
		determinizeStates();
		totalizeStates();
		minimizeStates();
	}

	private void totalizeStates() {
		State error = new State();
		for (State state : states) {
			if (isLive(state)) {
				for (ByteRange range : byteRanges) {
					if (state.nexts(range.from[0]).isEmpty()) {
						state.addTransition(new BytesTransition(state, range.from[0], range.to[0], error));
					}
				}
			}
		}
		init(start, error);
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
			for (ByteRange range : byteRanges) {
				byte representative = range.from[0];
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

	private BitSet reachingStates(BitSet bits, byte event) {
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
		Map<Set<State>, State> dStates = new HashMap<>();
		Queue<Set<State>> todo = new WorkSet<>();

		Set<State> startset = new HashSet<>();
		startset.add(start);
		todo.add(startset);
		State dStart = new State();
		dStates.put(startset, dStart);

		while (!todo.isEmpty()) {
			Set<State> current = todo.remove();
			State dState = dStates.get(current);
			transferAccept(current, dState);

			for (ByteRange range : byteRanges) {
				byte from = range.from[0];
				byte to = range.to[0];
				Set<State> nextset = new HashSet<>();
				for (State state : current) {
					for (OrdinaryTransition transition : state.nexts(from)) {
						nextset.add(transition.getTarget());
					}
				}
				State target = dStates.get(nextset);
				if (target == null) {
					todo.add(nextset);
					target = new State();
					dStates.put(nextset, target);
				}
				if (from == to) {
					dState.addTransition(new ByteTransition(dState, from, target));
				} else {
					dState.addTransition(new BytesTransition(dState, from, to, target));
				}
			}
		}
		init(dStart);
	}

	private void transferAccept(Set<State> states, State dState) {
		boolean accepting = false;
		boolean silent = true;
		for (State state : states) {
			accepting |= state.isAccepting();
			silent &= state.isSilent();
		}
		dState.setAccepting(accepting);
		dState.setSilent(silent);
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

	private static List<ByteRange> computeEquivalentByteRanges(State start) {
		ByteRangeAccumulator acc = new ByteRangeAccumulator();

		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (Transition transition : state.transitions()) {
				if (transition instanceof OrdinaryTransition) {
					OrdinaryTransition ordinaryTransition = (OrdinaryTransition) transition;
					acc.split(ordinaryTransition.getFrom(), ordinaryTransition.getTo());
				}
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
		clean();
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

				byte from = (byte) Math.min(from1, from2);
				byte to = (byte) Math.max(to1, to2);

				if (from == to) {
					return new ByteTransition(origin, from, target);
				} else {
					return new BytesTransition(origin, from, to, target);
				}
			}
		}
		return null;
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
		init(start);
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

	private static class ByteRangeAccumulator {

		private List<ByteRange> ranges;

		public ByteRangeAccumulator() {
			ranges = ArrayLists.of(new ByteRange((byte) 0x00, (byte) 0xff, 256));
		}

		public List<ByteRange> getRanges() {
			return ranges;
		}

		public void split(byte from, byte to) {
			for (int i = 0; i < ranges.size(); i++) {
				ByteRange currentRange = ranges.get(i);
				if (currentRange.contains(from) && currentRange.contains(to)) {
					i = replace(i, currentRange.splitAround(from, to));
				} else if (currentRange.contains(from)) {
					i = replace(i, currentRange.splitBefore(from));
				} else if (currentRange.contains(to)) {
					i = replace(i, currentRange.splitAfter(to));
				}
			}
		}

		public int replace(int i, List<ByteRange> replacement) {
			ranges.remove(i);
			ranges.addAll(i, replacement);
			return i + replacement.size() - 1;
		}

	}

}
