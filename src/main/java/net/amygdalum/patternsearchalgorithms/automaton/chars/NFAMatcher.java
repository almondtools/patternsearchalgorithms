package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static net.amygdalum.patternsearchalgorithms.automaton.chars.NFAMatcherState.of;

import net.amygdalum.util.io.CharProvider;

public class NFAMatcher {

	private CharProvider chars;
	private NFAMatcherState activeState;

	public NFAMatcher(NFA automaton, CharProvider chars) {
		this.chars = chars;
		this.activeState = of(automaton.getStart(), new Groups(), chars.current());
	}

	public boolean matches() {
		while (!chars.finished()) {
			char c = chars.next();
			activeState = activeState.next(c, chars.current());
		}
		return activeState.isAccepting(chars.current());
	}

}
