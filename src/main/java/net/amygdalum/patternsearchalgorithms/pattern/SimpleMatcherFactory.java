package net.amygdalum.patternsearchalgorithms.pattern;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;

import net.amygdalum.patternsearchalgorithms.dfa.DFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFABuilder;
import net.amygdalum.patternsearchalgorithms.nfa.NFAComponent;
import net.amygdalum.util.io.ByteProvider;

public class SimpleMatcherFactory implements MatcherFactory {

	private SearchMode mode;
	private DFA matcher;
	private NFA finder;

	public SimpleMatcherFactory(SearchMode mode, DFA matcher, NFA finder) {
		this.mode = mode;
		this.matcher = matcher;
		this.finder = finder;
	}

	public static SimpleMatcherFactory compile(NFA nfa, SearchMode mode) {
		return new SimpleMatcherFactory(mode, matcherFrom(nfa), finderFrom(nfa));
	}

	private static NFA finderFrom(NFA nfa) {
		NFABuilder builder = new NFABuilder(nfa.getCharset());
		
		NFAComponent base = builder.matchGroup(nfa.clone().asComponent(), 0);
		NFAComponent selfloop = builder.matchStarLoop(builder.match(MIN_VALUE, MAX_VALUE));
		NFAComponent search = builder.matchConcatenation(asList(selfloop, base));
		
		NFA finder = search.toFullNFA(nfa.getCharset());
		finder.prune();
		
		return finder;
	}

	private static DFA matcherFrom(NFA nfa) {
		return DFA.from(nfa);
	}

	@Override
	public Matcher newMatcher(ByteProvider input) {
		return new SimpleMatcher(mode, matcher, finder, input);
	}

}
