package net.amygdalum.patternsearchalgorithms.pattern.bytes;

import java.nio.charset.Charset;

import net.amygdalum.patternsearchalgorithms.automaton.bytes.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFABuilder;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFAComponent;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.patternsearchalgorithms.pattern.SearchMode;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.util.io.ByteProvider;

public class SimpleMatcherFactory implements MatcherFactory {

	private SearchMode mode;
	private Charset charset;
	private DFA matcher;
	private NFA grouper;

	public SimpleMatcherFactory(SearchMode mode, Charset charset) {
		this.mode = mode;
		this.charset = charset;
	}

	public static SimpleMatcherFactory compile(RegexNode node, Charset charset, SearchMode mode) {
		return new SimpleMatcherFactory(mode, charset).compile(node);
	}

	private SimpleMatcherFactory compile(RegexNode node) {
		this.matcher = matcherFrom(node);
		this.grouper = grouperFrom(node);
		
		return this;
	}

	private DFA matcherFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder(charset);
		return DFA.from(builder.build(node));
	}

	private NFA grouperFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = builder.matchGroup(node.accept(builder), 0);

		NFA grouper = builder.build(base);
		grouper.prune();

		return grouper;
	}

	@Override
	public Matcher newMatcher(ByteProvider input) {
		if (mode.findLongest()) {
			if (mode.findOverlapping()) {
				return new SimpleLongestOverlappingMatcher(matcher, grouper, input);
			} else {
				return new SimpleLongestNonOverlappingMatcher(matcher, grouper, input);
			}
		} else {
			if (mode.findOverlapping()) {
				return new SimpleAllOverlappingMatcher(matcher, grouper, input);
			} else {
				return new SimpleAllNonOverlappingMatcher(matcher, grouper, input);
			}
		}
	}

}
