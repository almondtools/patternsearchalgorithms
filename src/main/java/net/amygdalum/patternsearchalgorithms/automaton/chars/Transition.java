package net.amygdalum.patternsearchalgorithms.automaton.chars;

public interface Transition {

	State getTarget();
	
	State getOrigin();

	Action getAction();

	Transition asPrototype();
	
	Transition withOrigin(State state);
	
	Transition withTarget(State target);

	Transition withAction(Action action);

	Groups executeAction(Groups groups, long pos);

}
