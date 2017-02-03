package net.amygdalum.patternsearchalgorithms.nfa;

public interface Action {

	Groups applyTo(Groups groups, long pos);

}
