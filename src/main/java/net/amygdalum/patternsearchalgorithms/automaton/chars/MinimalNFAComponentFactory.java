package net.amygdalum.patternsearchalgorithms.automaton.chars;

public class MinimalNFAComponentFactory implements NFAComponentFactory {

	@Override
	public NFAComponent create(State start, State end) {
		end.setAccepting();
		NFA nfa = new NFA(start);
		nfa.determinize();
		
		return new NFAComponent(nfa.getStart(), getEnd(nfa));
	}

	private State getEnd(NFA nfa) {
		State[] accepting = nfa.accepting();
		switch (accepting.length) {
		case 0:
			return null;
		case 1:
			State existingend = accepting[0];
			existingend.setAccepting(false);
			return existingend;
		default:
			State joinend = new State();
			for (State accept : accepting) {
				accept.setAccepting(false);
				new EpsilonTransition(accept, joinend).connect();
			}
			return joinend;
		}
	}

}
