package net.amygdalum.patternsearchalgorithms.automaton.bytes;

public interface NFAComponentFactory {

	NFAComponent create(State start, State end);
	
}
