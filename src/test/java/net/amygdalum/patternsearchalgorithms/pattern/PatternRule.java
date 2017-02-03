package net.amygdalum.patternsearchalgorithms.pattern;


import org.junit.rules.TestRule;

import net.amygdalum.util.builders.Arrays;

public class PatternRule extends OptimizationTargetRule implements TestRule {
	
	public Pattern compile(String string, PatternOption... options) {
		return Pattern.compile(string, Arrays.<PatternOption>init(options.length + 1)
			.add(mode)
			.addAll(options)
			.build(new PatternOption[0]));
	}

}
