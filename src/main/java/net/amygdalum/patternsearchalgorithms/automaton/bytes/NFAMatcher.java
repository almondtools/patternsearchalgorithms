package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import static net.amygdalum.patternsearchalgorithms.automaton.bytes.NFAMatcherState.of;

import net.amygdalum.util.io.ByteProvider;

public class NFAMatcher {

	private ByteProvider bytes;
	private NFAMatcherState activeState;

	public NFAMatcher(NFA automaton, ByteProvider bytes) {
		this.bytes = bytes;
		this.activeState = of(automaton.getStart(), new Groups(), bytes.current());
	}

	public boolean matches() {
		while (!bytes.finished()) {
			byte b = bytes.next();
			activeState = activeState.next(b, bytes.current());
		}
		return activeState.isAccepting(bytes.current());
	}

}
