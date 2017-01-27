package net.amygdalum.patternsearchalgorithms.nfa;

public interface Transition {

	State getTarget();
	
	State getOrigin();

	Transition asPrototype();
	
	Transition withOrigin(State state);
	
	Transition withTarget(State target);

	Transition withAction(Action action);

	Groups executeAction(Groups groups);

}
