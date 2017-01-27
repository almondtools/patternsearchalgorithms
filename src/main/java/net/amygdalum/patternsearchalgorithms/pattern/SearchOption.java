package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.ArrayList;
import java.util.List;

public enum SearchOption implements PatternOption {

	LONGEST, NON_OVERLAPPING;

	public static SearchOption[] allOf(PatternOption[] options) {
		List<SearchOption> patternOptions = new ArrayList<>();
		for (PatternOption option : options) {
			if (option instanceof SearchOption) {
				patternOptions.add((SearchOption) option);
			}
		}
		return patternOptions.toArray(new SearchOption[0]);
	} 
}
