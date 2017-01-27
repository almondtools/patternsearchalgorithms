package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.ArrayList;
import java.util.List;

import net.amygdalum.regexparser.RegexParserOption;

public enum RegexOption implements PatternOption {

	DOT_ALL(RegexParserOption.DOT_ALL);
	
	private RegexParserOption option;

	private RegexOption(RegexParserOption option) {
		this.option = option;
	}

	public static RegexParserOption[] toRegexParserOptions(RegexOption[] regexOptions) {
		RegexParserOption[] regexParserOptions = new RegexParserOption[regexOptions.length];
		for (int i = 0; i < regexParserOptions.length; i++) {
			regexParserOptions[i] = regexOptions[i].option;
		}
		return regexParserOptions;
	}

	public static RegexOption[] allOf(PatternOption[] options) {
		List<RegexOption> patternOptions = new ArrayList<>();
		for (PatternOption option : options) {
			if (option instanceof RegexOption) {
				patternOptions.add((RegexOption) option);
			}
		}
		return patternOptions.toArray(new RegexOption[0]);
	} 
}
