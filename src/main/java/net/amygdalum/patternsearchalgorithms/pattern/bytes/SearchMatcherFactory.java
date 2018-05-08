package net.amygdalum.patternsearchalgorithms.pattern.bytes;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;

import java.nio.charset.Charset;

import net.amygdalum.patternsearchalgorithms.automaton.bytes.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFABuilder;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFAComponent;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.patternsearchalgorithms.pattern.SearchMode;
import net.amygdalum.regexparser.RegexNode;
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

	public static SearchMatcherFactory compile(RegexNode node, Charset charset, SearchMode mode) {
		return new SearchMatcherFactory(mode, finderFrom(node, charset), backmatcherFrom(node, charset), grouperFrom(node, charset));
	}

	private static DFA finderFrom(RegexNode node, Charset charset) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = node.accept(builder);
		NFAComponent selfloop = builder.matchStarLoop(builder.match(MIN_VALUE, MAX_VALUE)).silent();
		NFAComponent finder = builder.matchConcatenation(asList(selfloop, base));

		NFA finderNFA = finder.toFullNFA(charset);

		return DFA.from(finderNFA);
	}

	private static DFA backmatcherFrom(RegexNode node, Charset charset) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = node.accept(builder);
		NFAComponent reverse = base.reverse();

		NFA reverseNFA = reverse.toFullNFA(charset);

		return DFA.from(reverseNFA);
	}

	private static NFA grouperFrom(RegexNode node, Charset charset) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = builder.matchGroup(node.accept(builder), 0);

		NFA grouper = base.toFullNFA(charset);
		grouper.prune();

		return grouper;
	}

	@Override
	public Matcher newMatcher(ByteProvider input) {
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
