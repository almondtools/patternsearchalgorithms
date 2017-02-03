package net.amygdalum.patternsearchalgorithms.pattern;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;

import net.amygdalum.patternsearchalgorithms.dfa.DFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFABuilder;
import net.amygdalum.patternsearchalgorithms.nfa.NFAComponent;
import net.amygdalum.util.io.ByteProvider;

public class SearchMatcherFactory implements MatcherFactory {

	private SearchMode mode;
	private DFA finder;
	private DFA backmatcher;
	private NFA grouper;

	public SearchMatcherFactory(SearchMode mode, DFA finder, DFA backmatcher, NFA grouper) {
		this.mode = mode;
		this.finder = finder;
		this.backmatcher = backmatcher;
		this.grouper = grouper;
	}

	public static SearchMatcherFactory compile(NFA nfa, SearchMode mode) {
		return new SearchMatcherFactory(mode, finderFrom(nfa), backmatcherFrom(nfa), grouperFrom(nfa));
	}

	private static DFA finderFrom(NFA nfa) {
		NFABuilder builder = new NFABuilder(nfa.getCharset());
		
		NFAComponent base = nfa.clone().asComponent();
		NFAComponent selfloop = builder.matchStarLoop(builder.match(MIN_VALUE, MAX_VALUE)).silent();
		NFAComponent finder = builder.matchConcatenation(asList(selfloop, base));
		
		NFA finderNFA = finder.toFullNFA(nfa.getCharset());
		
		return DFA.from(finderNFA);
	}

	private static DFA backmatcherFrom(NFA nfa) {
		NFAComponent base = nfa.clone().asComponent();
		NFAComponent reverse = base.reverse();
		
		NFA reverseNFA = reverse.toFullNFA(nfa.getCharset());
		
		return DFA.from(reverseNFA);
	}

	private static NFA grouperFrom(NFA nfa) {
		NFABuilder builder = new NFABuilder(nfa.getCharset());
		
		NFAComponent base = builder.matchGroup(nfa.clone().asComponent(), 0);
		
		NFA grouper = base.toFullNFA(nfa.getCharset());
		grouper.prune();
		
		return grouper;
	}

	@Override
	public Matcher newMatcher(ByteProvider input) {
		return new SearchMatcher(mode, finder, backmatcher, grouper, input);
	}

}
