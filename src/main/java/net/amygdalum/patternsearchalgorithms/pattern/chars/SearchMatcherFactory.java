package net.amygdalum.patternsearchalgorithms.pattern.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;

import net.amygdalum.patternsearchalgorithms.automaton.chars.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.MinimalNFAComponentFactory;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFABuilder;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFAComponent;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.patternsearchalgorithms.pattern.SearchMode;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.util.io.CharProvider;

public class SearchMatcherFactory implements MatcherFactory {

	private SearchMode mode;
	private DFA finder;
	private DFA backmatcher;
	private NFA grouper;

	public SearchMatcherFactory(SearchMode mode) {
		this.mode = mode;
	}

	public static SearchMatcherFactory compile(RegexNode node, SearchMode mode) {
		return new SearchMatcherFactory(mode).compile(node);
	}

	private SearchMatcherFactory compile(RegexNode node) {
		this.finder = finderFrom(node);
		this.backmatcher = backmatcherFrom(node);
		this.grouper = grouperFrom(node);

		return this;
	}

	private DFA finderFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder(new MinimalNFAComponentFactory());

		NFAComponent base = node.accept(builder);
		NFAComponent selfloop = builder.matchStarLoop(builder.match(MIN_VALUE, MAX_VALUE)).silent();
		NFAComponent finder = builder.matchConcatenation(asList(selfloop, base));

		NFA nfa = builder.build(finder);
		return DFA.from(nfa);
	}

	private DFA backmatcherFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder(new MinimalNFAComponentFactory());

		NFAComponent base = node.accept(builder);
		NFAComponent reverse = base.reverse();

		NFA nfa = builder.build(reverse);
		return DFA.from(nfa);
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
