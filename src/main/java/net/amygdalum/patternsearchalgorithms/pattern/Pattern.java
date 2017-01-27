package net.amygdalum.patternsearchalgorithms.pattern;

import java.nio.charset.Charset;

import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFABuilder;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.regexparser.RegexParser;
import net.amygdalum.regexparser.RegexParserOption;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.io.StringByteProvider;

public class Pattern {

	private String pattern;
	private MatcherFactory factory;
	private Charset charset;

	private Pattern(String pattern, MatcherFactory factory, Charset charset) {
		this.pattern = pattern;
		this.factory = factory;
		this.charset = charset;
	}

	public String pattern() {
		return pattern;
	}

	public static Pattern compile(String pattern, PatternOption... options) {
		CharsetOption charset = CharsetOption.firstOf(options);
		RegexOption[] regexOptions = RegexOption.allOf(options);

		NFA nfa = buildNFAFrom(pattern, charset, regexOptions);

		OptimizationTarget optimizations = OptimizationTarget.bestOf(options);
		SearchOption[] search = SearchOption.allOf(options);

		MatcherFactory factory = buildFactory(nfa, charset, optimizations, search);

		return new Pattern(pattern, factory, charset.getCharset());
	}

	private static MatcherFactory buildFactory(NFA nfa, CharsetOption charset, OptimizationTarget optimizationTarget, SearchOption[] search) {
		switch (optimizationTarget) {
		case GROUPS:
			return GroupMatcherFactory.compile(nfa, search);
		case SEARCH:
			return SearchMatcherFactory.compile(nfa, search);
		case PREFIX:
			return PrefixMatcherFactory.compile(nfa);
		case MATCH:
		default:
			return SimpleMatcherFactory.compile(nfa);
		}
	}

	public static NFA buildNFAFrom(String pattern, CharsetOption charset, RegexOption[] regexOptions) {
		RegexParserOption[] parserOptions = RegexOption.toRegexParserOptions(regexOptions);
		RegexParser parser = new RegexParser(pattern, parserOptions);
		RegexNode node = parser.parse();
		return new NFABuilder(node, charset.getCharset()).build();
	}

	public Matcher matcher(String input) {
		ByteProvider bytes = new StringByteProvider(input, 0, charset);
		return factory.newMatcher(bytes);
	}

}
