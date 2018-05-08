package net.amygdalum.patternsearchalgorithms.pattern;

import java.nio.charset.Charset;

import net.amygdalum.patternsearchalgorithms.pattern.bytes.MatcherFactory;
import net.amygdalum.patternsearchalgorithms.pattern.bytes.SearchMatcherFactory;
import net.amygdalum.patternsearchalgorithms.pattern.bytes.SimpleMatcherFactory;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.regexparser.RegexParser;
import net.amygdalum.regexparser.RegexParserOption;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.io.StringByteProvider;

class BytePattern extends Pattern {

	private String pattern;
	private MatcherFactory factory;
	private Charset charset;

	BytePattern(String pattern, MatcherFactory factory, Charset charset) {
		this.pattern = pattern;
		this.factory = factory;
		this.charset = charset;
	}

	@Override
	public String pattern() {
		return pattern;
	}

	@Override
	public Matcher matcher(String input) {
		ByteProvider bytes = new StringByteProvider(input, 0, charset);
		return factory.newMatcher(bytes);
	}
	
	@Override
	public Matcher matcher(CharProvider input) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Matcher matcher(byte[] input) {
		return factory.newMatcher(new StringByteProvider(input, 0, charset));
	}
	
	@Override
	public Matcher matcher(ByteProvider input) {
		return factory.newMatcher(input);
	}

	static Pattern compile(String pattern, CharsetOption charset, RegexOption[] regexOptions, OptimizationTarget optimizations, SearchMode mode) {
		RegexNode node = buildNFAFrom(pattern, regexOptions);

		MatcherFactory factory = buildFactory(node, charset, optimizations, mode);

		return new BytePattern(pattern, factory, charset.getCharset());
	}

	private static MatcherFactory buildFactory(RegexNode node, CharsetOption charset, OptimizationTarget optimizationTarget, SearchMode mode) {
		switch (optimizationTarget) {
		case SEARCH:
			return SearchMatcherFactory.compile(node, charset.getCharset(), mode);
		case MATCH:
		default:
			return SimpleMatcherFactory.compile(node, charset.getCharset(),  mode);
		}
	}

	public static RegexNode buildNFAFrom(String pattern, RegexOption[] regexOptions) {
		RegexParserOption[] parserOptions = RegexOption.toRegexParserOptions(regexOptions);
		RegexParser parser = new RegexParser(pattern, parserOptions);
		return parser.parse();
	}

}
