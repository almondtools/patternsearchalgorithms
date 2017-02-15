package net.amygdalum.patternsearchalgorithms.automaton.bytes;

public interface Action {

	Groups applyTo(Groups groups, long pos);

}
