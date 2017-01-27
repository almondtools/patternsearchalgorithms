package net.amygdalum.patternsearchalgorithms.pattern;

public enum OptimizationTarget implements PatternOption {

	MATCH, PREFIX, SEARCH, GROUPS; 

	public static OptimizationTarget bestOf(PatternOption[] options) {
		OptimizationTarget target = MATCH;
		for (PatternOption option : options) {
			if (option instanceof OptimizationTarget) {
				OptimizationTarget current = (OptimizationTarget) option;
				if (target.ordinal() < current.ordinal()) {
					target = current;
				}
			}
		}
		return target;
	} 
	
}
