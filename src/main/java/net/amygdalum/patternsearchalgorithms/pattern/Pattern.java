package net.amygdalum.patternsearchalgorithms.pattern;

import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.io.CharProvider;

public abstract class Pattern {

	public static Pattern compile(String pattern, PatternOption... options) {
		CharsetOption charset = CharsetOption.firstOf(options);
		RegexOption[] regexOptions = RegexOption.allOf(options);

		OptimizationTarget optimizations = OptimizationTarget.bestOf(options);
		SearchMode mode = SearchMode.firstOf(options);

		if (charset == null) {
			return CharPattern.compile(pattern, regexOptions, optimizations, mode);
		} else {
			return BytePattern.compile(pattern, charset, regexOptions, optimizations, mode);
		}
	}

	public abstract Matcher matcher(String input);
	public abstract Matcher matcher(CharProvider input);

	public abstract Matcher matcher(byte[] input);
	public abstract Matcher matcher(ByteProvider input);


}
