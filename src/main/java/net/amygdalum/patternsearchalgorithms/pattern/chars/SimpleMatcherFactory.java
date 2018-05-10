package net.amygdalum.patternsearchalgorithms.pattern.chars;

import net.amygdalum.patternsearchalgorithms.automaton.chars.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFABuilder;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFAComponent;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.patternsearchalgorithms.pattern.SearchMode;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.util.io.CharProvider;

public class SimpleMatcherFactory implements MatcherFactory {

	private SearchMode mode;
	private DFA matcher;
	private NFA grouper;

	public SimpleMatcherFactory(SearchMode mode) {
		this.mode = mode;
	}

	public static SimpleMatcherFactory compile(RegexNode node, SearchMode mode) {
		return new SimpleMatcherFactory(mode).compile(node);
	}

	private SimpleMatcherFactory compile(RegexNode node) {
		this.matcher = matcherFrom(node);
		this.grouper = grouperFrom(node);

		return this;
	}

	private DFA matcherFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder();
		return DFA.from(builder.build(node));
	}

	private NFA grouperFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder();

		NFAComponent base = builder.matchGroup(node.accept(builder), 0);

		NFA grouper = builder.build(base);
		grouper.prune();

		return grouper;
	}

	@Override
	public Matcher newMatcher(CharProvider input) {
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
