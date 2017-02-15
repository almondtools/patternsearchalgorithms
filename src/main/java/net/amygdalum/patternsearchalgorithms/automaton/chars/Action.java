package net.amygdalum.patternsearchalgorithms.automaton.chars;

public interface Action {

	Groups applyTo(Groups groups, long pos);

}
