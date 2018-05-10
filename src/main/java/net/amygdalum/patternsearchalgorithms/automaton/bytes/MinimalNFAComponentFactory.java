package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import java.nio.charset.Charset;

public class MinimalNFAComponentFactory implements NFAComponentFactory {

	private Charset charset;
	
	public MinimalNFAComponentFactory(Charset charset) {
		this.charset = charset;
	}

	@Override
	public NFAComponent create(State start, State end) {
		end.setAccepting();
		NFA nfa = new NFA(start, charset);
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
