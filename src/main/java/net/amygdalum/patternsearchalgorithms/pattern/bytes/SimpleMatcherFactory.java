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
	private DFA matcher;
	private NFA grouper;

	public SimpleMatcherFactory(SearchMode mode, DFA matcher, NFA grouper) {
		this.mode = mode;
		this.matcher = matcher;
		this.grouper = grouper;
	}

	public static SimpleMatcherFactory compile(RegexNode node, Charset charset, SearchMode mode) {
		return new SimpleMatcherFactory(mode, matcherFrom(node, charset), grouperFrom(node, charset));
	}

	public static DFA matcherFrom(RegexNode node, Charset charset) {
		NFABuilder builder = new NFABuilder(charset);
		return DFA.from(builder.build(node));
	}

	private static NFA grouperFrom(RegexNode node, Charset charset) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = builder.matchGroup(node.accept(builder), 0);

		NFA finder = base.toFullNFA(charset);
		finder.prune();

		return finder;
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
