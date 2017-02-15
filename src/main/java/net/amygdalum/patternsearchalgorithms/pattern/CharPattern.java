package net.amygdalum.patternsearchalgorithms.pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

import net.amygdalum.patternsearchalgorithms.automaton.chars.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFABuilder;
import net.amygdalum.patternsearchalgorithms.pattern.chars.MatcherFactory;
import net.amygdalum.patternsearchalgorithms.pattern.chars.SearchMatcherFactory;
import net.amygdalum.patternsearchalgorithms.pattern.chars.SimpleMatcherFactory;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.regexparser.RegexParser;
import net.amygdalum.regexparser.RegexParserOption;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.io.StringCharProvider;

class CharPattern extends Pattern {

	private String pattern;
	private MatcherFactory factory;

	CharPattern(String pattern, MatcherFactory factory) {
		this.pattern = pattern;
		this.factory = factory;
	}

	public String pattern() {
		return pattern;
	}

	@Override
	public Matcher matcher(String input) {
		CharProvider chars = new StringCharProvider(input, 0);
		return factory.newMatcher(chars);
	}
	
	@Override
	public Matcher matcher(CharProvider input) {
		return factory.newMatcher(input);
	}

	@Override
	public Matcher matcher(byte[] input) {
		CharProvider chars = new StringCharProvider(new String(input, UTF_8), 0);
		return factory.newMatcher(chars);
	}
	
	@Override
	public Matcher matcher(ByteProvider input) {
		throw new UnsupportedOperationException();
	}

	static Pattern compile(String pattern, RegexOption[] regexOptions, OptimizationTarget optimizations, SearchMode mode) {
		NFA nfa = buildNFAFrom(pattern, regexOptions);

		MatcherFactory factory = buildFactory(nfa, optimizations, mode);

		return new CharPattern(pattern, factory);
	}


	private static MatcherFactory buildFactory(NFA nfa, OptimizationTarget optimizationTarget, SearchMode mode) {
		switch (optimizationTarget) {
		case SEARCH:
			return SearchMatcherFactory.compile(nfa, mode);
		case MATCH:
		default:
			return SimpleMatcherFactory.compile(nfa, mode);
		}
	}

	public static NFA buildNFAFrom(String pattern, RegexOption[] regexOptions) {
		RegexParserOption[] parserOptions = RegexOption.toRegexParserOptions(regexOptions);
		RegexParser parser = new RegexParser(pattern, parserOptions);
		RegexNode node = parser.parse();
		return new NFABuilder().build(node);
	}

}
