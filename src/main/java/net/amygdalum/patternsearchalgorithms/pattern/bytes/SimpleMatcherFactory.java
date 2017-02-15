package net.amygdalum.patternsearchalgorithms.pattern.bytes;

import net.amygdalum.patternsearchalgorithms.automaton.bytes.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFABuilder;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFAComponent;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.patternsearchalgorithms.pattern.SearchMode;
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

	public static SimpleMatcherFactory compile(NFA nfa, SearchMode mode) {
		return new SimpleMatcherFactory(mode, matcherFrom(nfa), grouperFrom(nfa));
	}

	public static DFA matcherFrom(NFA nfa) {
		return DFA.from(nfa);
	}

	private static NFA grouperFrom(NFA nfa) {
		NFABuilder builder = new NFABuilder(nfa.getCharset());

		NFAComponent base = builder.matchGroup(nfa.clone().asComponent(), 0);

		NFA finder = base.toFullNFA(nfa.getCharset());
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
