package net.amygdalum.patternsearchalgorithms.pattern.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;

import net.amygdalum.patternsearchalgorithms.automaton.chars.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFABuilder;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFAComponent;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.patternsearchalgorithms.pattern.SearchMode;
import net.amygdalum.util.io.CharProvider;

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
		NFABuilder builder = new NFABuilder();
		
		NFAComponent base = nfa.clone().asComponent();
		NFAComponent selfloop = builder.matchStarLoop(builder.match(MIN_VALUE, MAX_VALUE)).silent();
		NFAComponent finder = builder.matchConcatenation(asList(selfloop, base));
		
		NFA finderNFA = finder.toFullNFA();
		
		return DFA.from(finderNFA);
	}

	private static DFA backmatcherFrom(NFA nfa) {
		NFAComponent base = nfa.clone().asComponent();
		NFAComponent reverse = base.reverse();
		
		NFA reverseNFA = reverse.toFullNFA();
		
		return DFA.from(reverseNFA);
	}

	private static NFA grouperFrom(NFA nfa) {
		NFABuilder builder = new NFABuilder();
		
		NFAComponent base = builder.matchGroup(nfa.clone().asComponent(), 0);
		
		NFA grouper = base.toFullNFA();
		grouper.prune();
		
		return grouper;
	}

	@Override
	public Matcher newMatcher(CharProvider input) {
		if (mode.findLongest()) {
			if (mode.findOverlapping()) {
				return new SearchLongestOverlappingMatcher(finder, backmatcher, grouper, input);
			} else {
				return new SearchLongestNonOverlappingMatcher(finder, backmatcher, grouper, input);
			}
		} else {
			if (mode.findOverlapping()) {
				return new SearchAllOverlappingMatcher(finder, backmatcher, grouper, input);
			} else {
				return new SearchAllNonOverlappingMatcher(finder, backmatcher, grouper, input);
			}
		}
	}

}