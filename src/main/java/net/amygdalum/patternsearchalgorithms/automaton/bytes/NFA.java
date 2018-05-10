package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.amygdalum.util.builders.ArrayLists;
import net.amygdalum.util.text.ByteRange;
import net.amygdalum.util.worklist.WorkSet;

public class NFA implements Cloneable {

	private Charset charset;
	private State start;
	private List<ByteRange> byteRanges;
	private State[] states;
	private int accepting;

	public NFA(State start, Charset charset) {
		this.charset = charset;
		init(start);
	}

	private void init(State start) {
		this.start = start;
		this.byteRanges = computeEquivalentByteRanges(start);
		this.states = clean(start, null);
		this.accepting = order(states);
	}

	private void init(State start, State error) {
		this.start = start;
		this.byteRanges = computeEquivalentByteRanges(start);
		this.states = clean(start, error);
		this.accepting = order(states);
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

	public State[] accepting() {
		return Arrays.copyOfRange(states, accepting, states.length);
	}

	private static State[] clean(State start, State error) {
		WorkSet<State> todo = new WorkSet<>();
		todo.add(start);
		Set<State> dead = new HashSet<>();
		WorkSet<State> live = new WorkSet<>();
		while (!todo.isEmpty()) {
			State state = todo.remove();
			if (state.isAccepting()) {
				live.add(state);
			} else {
				dead.add(state);
			}
			for (Transition transition : state.out()) {
				todo.add(transition.getTarget());
			}
		}
		while (!live.isEmpty()) {
			State current = live.remove();
			for (Transition liveTransition : current.in()) {
				State nextLive = liveTransition.getOrigin();
				live.add(nextLive);
				dead.remove(nextLive);
			}
		}

		dead.remove(start);
		live.remove(start);
		live.getDone().add(start);
		if (error != null) {
			dead.remove(error);
			live.remove(error);
			live.getDone().add(error);
		}

		for (State current : dead) {
			current.disconnect();
		}

		return live.getDone().toArray(new State[0]);
	}

	private static int order(State[] states) {
		int left = 0;
		int right = states.length - 1;
		while (left <= right) {
			while (left < states.length && !states[left].isAccepting()) {
				states[left].setId(left);
				left++;
			}
			while (right >= 0 && states[right].isAccepting()) {
				states[right].setId(right);
				right--;
			}
			if (left < right) {
				State temp = states[right];
				states[right] = states[left];
				states[left] = temp;
			}
		}
		return right + 1;
	}

	public void prune() {
		eliminateTrivialEpsilons();
		mergeTransitions();
	}

	public void determinize() {
		eliminateAllEpsilons();
		mergeTransitions();
		determinizeStates();
		totalizeStates();
		minimizeStates();
	}

	private void totalizeStates() {
		State error = new State();
		Queue<State> todo = new WorkSet<>();
		todo.add(error);
		todo.add(start);
		while (!todo.isEmpty()) {
			State current = todo.remove();
			List<ByteRange> missingRanges = new LinkedList<>(byteRanges);
			for (Transition transition : current.out()) {
				todo.add(transition.getTarget());
				if (transition instanceof OrdinaryTransition) {
					byte b = ((OrdinaryTransition) transition).getFrom();
					Iterator<ByteRange> iterator = missingRanges.iterator();
					while (iterator.hasNext()) {
						ByteRange check = iterator.next();
						if (check.contains(b)) {
							iterator.remove();
							break;
						}
					}
				}
			}
			for (ByteRange range : missingRanges) {
				new BytesTransition(current, range.from[0], range.to[0], error).connect();
			}
		}
		init(start, error);
	}

	private void minimizeStates() {
		List<Set<State>> partitions = new LinkedList<>();
		Queue<Set<State>> todo = new LinkedList<>();

		SplitPartition initialPartition = initialPartition();

		if (initialPartition.min.isEmpty()) {
			partitions.add(initialPartition.max);
		} else {
			partitions.add(initialPartition.max);
			partitions.add(initialPartition.min);
			todo.add(initialPartition.min);
		}

		while (!todo.isEmpty()) {
			Set<State> current = todo.remove();
			for (ByteRange charRange : byteRanges) {
				Set<State> origins = origins(current, charRange);
				ListIterator<Set<State>> partitionIterator = partitions.listIterator();
				while (partitionIterator.hasNext()) {
					Set<State> partition = partitionIterator.next();
					SplitPartition splitPartition = split(partition, origins);
					if (splitPartition.min.isEmpty()) {
						continue;
					}
					partitionIterator.set(splitPartition.max);
					partitionIterator.add(splitPartition.min);
					if (todo.contains(partition)) {
						todo.remove(partition);
						todo.add(splitPartition.max);
						todo.add(splitPartition.min);
					} else {
						todo.add(splitPartition.min);
					}
				}
			}
		}

		State newstart = digest(partitions);
		init(newstart);
	}

	private State digest(List<Set<State>> partitions) {
		Map<State, State> mapping = new IdentityHashMap<>();
		State start = null;
		for (Set<State> partition : partitions) {
			State state = new State();
			for (State partstate : partition) {
				mapping.put(partstate, state);
				if (partstate.isAccepting()) {
					state.setAccepting();
				}
				if (!partstate.isSilent()) {
					state.setSilent(false);
				}
				if (partstate == this.start) {
					start = state;
				}
			}
		}

		for (Set<State> partition : partitions) {
			State representative = partition.iterator().next();

			for (Transition transition : representative.out()) {
				State origin = transition.getOrigin();
				State mappedOrigin = mapping.get(origin);
				State target = transition.getTarget();
				State mappedTarget = mapping.get(target);
				transition.asPrototype().withOrigin(mappedOrigin).withTarget(mappedTarget).connect();
			}
		}
		return start;
	}

	private SplitPartition split(Set<State> partition, Set<State> splitter) {
		Set<State> intersection = new HashSet<>(partition.size());
		Set<State> remainder = new HashSet<>(partition.size());
		for (State state : partition) {
			if (splitter.contains(state)) {
				intersection.add(state);
			} else {
				remainder.add(state);
			}
		}
		return new SplitPartition(intersection, remainder);
	}

	private Set<State> origins(Set<State> states, ByteRange byteRange) {
		Set<State> in = new HashSet<>();
		for (State state : states) {
			for (Transition transition : state.in()) {
				if (transition instanceof OrdinaryTransition && ((OrdinaryTransition) transition).accepts(byteRange.from[0])) {
					in.add(transition.getOrigin());
				}
			}
		}
		return in;
	}

	private SplitPartition initialPartition() {
		Set<State> accept = new HashSet<>(states.length);
		Set<State> nonaccept = new HashSet<>(states.length);
		for (State state : states) {
			if (state.isAccepting()) {
				accept.add(state);
			} else {
				nonaccept.add(state);
			}
		}
		return new SplitPartition(accept, nonaccept);
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
					for (Transition transition : state.out()) {
						if (transition instanceof OrdinaryTransition && ((OrdinaryTransition) transition).accepts(from))
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
					new ByteTransition(dState, from, target).connect();
				} else {
					new BytesTransition(dState, from, to, target).connect();
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

	private static List<ByteRange> computeEquivalentByteRanges(State start) {
		ByteRangeAccumulator acc = new ByteRangeAccumulator();

		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (Transition transition : state.out()) {
				if (transition instanceof OrdinaryTransition) {
					OrdinaryTransition ordinaryTransition = (OrdinaryTransition) transition;
					acc.split(ordinaryTransition.getFrom(), ordinaryTransition.getTo());
				}
				todo.add(transition.getTarget());
			}
		}
		return acc.getRanges();
	}

	private void mergeTransitions() {
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			SortedSet<Transition> transitions = new TreeSet<>(new TransitionComparator());
			transitions.addAll(state.out());

			Transition last = null;
			for (Transition transition : transitions) {
				if (last == null) {
					last = transition;
				} else {
					Transition joined = tryJoin(last, transition);
					if (joined == null) {
						if (!transitions.contains(last)) {
							last.connect();
						}
						last = transition;
					} else if (joined == last) {
						transition.remove();
					} else if (joined == transition) {
						if (transitions.contains(last)) {
							last.remove();
						}
						last = joined;
					} else {
						if (transitions.contains(last)) {
							last.remove();
						}
						transition.remove();
						last = joined;
					}
				}
			}
			if (last != null && !transitions.contains(last)) {
				last.connect();
			}
		}
		clean(start, null);
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

				if (from1 == from && to1 == to) {
					return ot1;
				} else if (from2 == from && to2 == to) {
					return ot2;
				} else {
					return new BytesTransition(origin, from, to, target);
				}
			}
		}
		return null;
	}

	private void eliminateTrivialEpsilons() {
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (Transition transition : state.out()) {
				todo.add(transition.getTarget());
			}
			for (EpsilonTransition epsilon : transitiveEpsilons(state)) {
				State origin = epsilon.getOrigin();
				if (origin == state) {
					epsilon.remove();
				}
				State target = epsilon.getTarget();
				if (target.isAccepting()) {
					state.setAccepting();
				}
				if (!target.isSilent()) {
					state.setSilent(false);
				}
				for (Transition transition : target.out()) {
					if (transition instanceof OrdinaryTransition) {
						transition.asPrototype().withOrigin(state).withTarget(transition.getTarget()).connect();
					}
				}
				for (Transition transition : target.out()) {
					if (transition instanceof EpsilonTransition) {
						Action action = transition.getAction();
						if (action != null) {
							transition.asPrototype().withOrigin(state).withTarget(transition.getTarget()).withAction(action).connect();
						}
					}
				}
			}
		}
		init(start);
	}

	private Set<EpsilonTransition> transitiveEpsilons(State state) {

		WorkSet<EpsilonTransition> todo = new WorkSet<>();
		for (Transition transition : state.out()) {
			if (transition instanceof EpsilonTransition) {
				todo.add((EpsilonTransition) transition);
			}
		}
		while (!todo.isEmpty()) {
			EpsilonTransition current = todo.remove();
			if (current.getAction() != null) {
				todo.remove(current);
				continue;
			}
			State target = current.getTarget();
			for (Transition transition : target.out()) {
				if (transition instanceof EpsilonTransition) {
					todo.add((EpsilonTransition) transition);
				}
			}
		}
		return todo.getDone();
	}

	private void eliminateAllEpsilons() {
		Queue<EpsilonTransition> epsilons = new LinkedList<>();
		Queue<State> todo = new WorkSet<>();
		todo.add(start);
		while (!todo.isEmpty()) {
			State state = todo.remove();
			for (Transition transition : state.out()) {
				todo.add(transition.getTarget());
				if (transition instanceof EpsilonTransition) {
					epsilons.add((EpsilonTransition) transition);
				}
			}
		}

		WorkSet<EpsilonTransition> propagateEpsilons = new WorkSet<>();
		propagateEpsilons.addAll(epsilons);
		while (!propagateEpsilons.isEmpty()) {
			EpsilonTransition epsilon = propagateEpsilons.remove();
			Set<EpsilonTransition> done = propagateStates(epsilon);
			propagateEpsilons.removeAll(done);
			propagateEpsilons.getDone().addAll(done);
		}

		while (!epsilons.isEmpty()) {
			EpsilonTransition epsilon = epsilons.remove();
			State origin = epsilon.getOrigin();
			State target = epsilon.getTarget();
			int in = origin.in().size();
			int out = target.out().size();
			if (origin == start) {
				eliminateForward(epsilon);
			} else if (in >= out) {
				eliminateForward(epsilon);
			} else {
				eliminateBackward(epsilon);
			}
		}

		init(start);
	}

	private Set<EpsilonTransition> propagateStates(EpsilonTransition epsilon) {
		boolean accepting = false;
		boolean silent = true;

		Set<EpsilonTransition> propagated = new HashSet<>();
		WorkSet<State> eclosure = new WorkSet<>();
		eclosure.add(epsilon.getOrigin());
		eclosure.add(epsilon.getTarget());
		while (!eclosure.isEmpty()) {
			State next = eclosure.remove();
			accepting |= next.isAccepting();
			silent &= next.isSilent();

			for (Transition transition : next.out()) {
				if (transition instanceof EpsilonTransition) {
					propagated.add((EpsilonTransition) transition);
					eclosure.add(transition.getTarget());
				}
			}
			for (Transition transition : next.in()) {
				if (transition instanceof EpsilonTransition) {
					propagated.add((EpsilonTransition) transition);
					eclosure.add(transition.getOrigin());
				}
			}
		}

		for (State state : eclosure.getDone()) {
			state.setAccepting(accepting);
			state.setSilent(silent);
		}

		return propagated;
	}

	private void eliminateForward(EpsilonTransition epsilon) {
		State origin = epsilon.getOrigin();
		WorkSet<State> targets = new WorkSet<>();
		targets.add(epsilon.getTarget());
		while (!targets.isEmpty()) {
			State next = targets.remove();
			for (Transition transition : next.out()) {
				if (transition instanceof OrdinaryTransition) {
					transition.asPrototype().withOrigin(origin).withTarget(transition.getTarget()).connect();
				} else if (transition instanceof EpsilonTransition) {
					targets.add(transition.getTarget());
				}
			}
		}
		epsilon.remove();
		if (origin.out().isEmpty() && !origin.isAccepting()) {
			origin.disconnect();
		}
		for (State target : targets.getDone()) {
			if (target.in().isEmpty() && target != start) {
				target.disconnect();
			}
		}
	}

	private void eliminateBackward(EpsilonTransition epsilon) {
		State target = epsilon.getTarget();
		WorkSet<State> origins = new WorkSet<>();
		origins.add(epsilon.getOrigin());
		while (!origins.isEmpty()) {
			State next = origins.remove();
			for (Transition transition : next.in()) {
				if (transition instanceof OrdinaryTransition) {
					transition.asPrototype().withOrigin(transition.getOrigin()).withTarget(target).connect();
				} else if (transition instanceof EpsilonTransition) {
					origins.add(transition.getOrigin());
				}
			}
		}
		epsilon.remove();
		if (target.in().isEmpty() && target != start) {
			target.disconnect();
		}
		for (State origin : origins.getDone()) {
			if (origin.out().isEmpty()) {
				origin.disconnect();
			}
		}
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

	private static class SplitPartition {
		public Set<State> max;
		public Set<State> min;

		public SplitPartition(Set<State> intersection, Set<State> remainder) {
			this.max = intersection.size() > remainder.size() ? intersection : remainder;
			this.min = intersection.size() <= remainder.size() ? intersection : remainder;
		}

	}

}
