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
	private Charset charset;
	private DFA finder;
	private DFA backmatcher;
	private NFA grouper;

	private SearchMatcherFactory(SearchMode mode, Charset charset) {
		this.mode = mode;
		this.charset = charset;
	}

	public static SearchMatcherFactory compile(RegexNode node, Charset charset, SearchMode mode) {
		return new SearchMatcherFactory(mode, charset).compile(node);
	}

	private SearchMatcherFactory compile(RegexNode node) {
		this.finder = finderFrom(node);
		this.backmatcher = backmatcherFrom(node);
		this.grouper = grouperFrom(node);

		return this;
	}

	private DFA finderFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = node.accept(builder);
		NFAComponent selfloop = builder.matchStarLoop(builder.match(MIN_VALUE, MAX_VALUE)).silent();
		NFAComponent finder = builder.matchConcatenation(asList(selfloop, base));

		return DFA.from(builder.build(finder));
	}

	private DFA backmatcherFrom(RegexNode node) {
		NFABuilder builder = new NFABuilder(charset);

		NFAComponent base = node.accept(builder);
		NFAComponent reverse = base.reverse();

		return DFA.from(builder.build(reverse));
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
