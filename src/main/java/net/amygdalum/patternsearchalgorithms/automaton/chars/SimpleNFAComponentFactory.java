package net.amygdalum.patternsearchalgorithms.automaton.chars;

public class SimpleNFAComponentFactory implements NFAComponentFactory {

	@Override
	public NFAComponent create(State start, State end) {
		return new NFAComponent(start, end);
	}

}
