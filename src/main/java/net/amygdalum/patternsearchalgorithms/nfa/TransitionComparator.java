package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.Comparator;

public class TransitionComparator implements Comparator<Transition> {

	@Override
	public int compare(Transition t1, Transition t2) {
		int compare = compareTargets(t1.getTarget(), t2.getTarget());
		if (compare != 0) {
			return compare;
		} else if (t1 instanceof EpsilonTransition && t2 instanceof EpsilonTransition) {
			return 0;
		} else if (t1 instanceof OrdinaryTransition && t2 instanceof EpsilonTransition) {
			return 1;
		} else if (t1 instanceof EpsilonTransition && t2 instanceof OrdinaryTransition) {
			return -1;
		} else if (t1 instanceof OrdinaryTransition && t2 instanceof OrdinaryTransition) {
			OrdinaryTransition ot1 = (OrdinaryTransition) t1;
			OrdinaryTransition ot2 = (OrdinaryTransition) t2;
			if (ot1.getFrom() < ot2.getFrom()) {
				return -1;
			} else if (ot1.getFrom() > ot2.getFrom()) {
				return 1;
			} else if (ot1.getTo() < ot2.getTo()) {
				return -1;
			} else if (ot1.getTo() > ot2.getTo()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	private int compareTargets(State t1, State t2) {
		return Integer.compare(t1.getId(), t2.getId());
	}

}
