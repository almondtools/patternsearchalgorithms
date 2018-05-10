package net.amygdalum.patternsearchalgorithms.automaton.chars;

public interface NFAComponentFactory {

	NFAComponent create(State start, State end);
	
}
