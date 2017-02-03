package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import net.amygdalum.patternsearchalgorithms.dfa.DFA;
import net.amygdalum.patternsearchalgorithms.nfa.Groups;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFAMatcherState;
import net.amygdalum.util.builders.TreeSets;
import net.amygdalum.util.io.ByteProvider;

public class SimpleMatcher implements Matcher {

	private SearchMode mode;
	private DFA matcher;
	private NFA finder;
	private ByteProvider input;
	private long start;

	private int matcherState;
	private NFAMatcherState finderState;
	private SortedSet<Groups> groups;
	private SortedSet<Groups> nextgroups;

	public SimpleMatcher(SearchMode mode, DFA matching, NFA finding, ByteProvider input) {
		this.mode = mode;
		this.matcher = matching;
		this.finder = finding;
		this.input = input;
		this.start = input.current();
		this.matcherState = -2;
		this.finderState = null;
		this.groups = null;
		this.nextgroups = null;
	}

	@Override
	public boolean matches() {
		if (matcherState == -2) {
			matcherState = matcher.start;
		}
		while (!input.finished() && matcherState >= 0) {
			byte b = input.next();
			matcherState = matcher.next(matcherState, b);
		}
		if (matcher.accept(matcherState)) {
			groups = TreeSets.of(new Groups(start, input.current()));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean prefixes() {
		if (matcherState == -2) {
			matcherState = matcher.start;
		}
		if (matcher.accept(matcherState)) {
			groups = TreeSets.of(new Groups(start, input.current()));
			return true;
		}
		while (!input.finished() && matcherState >= 0) {
			byte b = input.next();
			matcherState = matcher.next(matcherState, b);
			if (matcher.accept(matcherState)) {
				groups = TreeSets.of(new Groups(start, input.current()));
				if (mode.findAll()) {
					return true;
				}
			}
		}
		if (mode.findLongest() && groups != null) {
			return true;
		} else if (matcher.accept(matcherState)) {
			groups = TreeSets.of(new Groups(start, input.current()));
			return true;
		}
		return false;
	}

	@Override
	public boolean find() {
		groups = nextgroups;
		if (finderState == null) {
			finderState = NFAMatcherState.of(finder.getStart(), new Groups(), input.current());
		}
		while (!input.finished()) {
			byte b = input.next();
			long current = input.current();
			finderState = finderState.next(b, current);
			if (finderState.isAccepting(current)) {
				if (mode.findAll()) {
					groups = finderState.getGroups();
					nextgroups = null;
					if (mode.findNonOverlapping()) {
						finderState = null;
					}
					return true;
				} else if (longestMatchDetected(finderState.getGroups())) {
					if (mode.findNonOverlapping()) {
						finderState = finderState.cancelOverlapping(groups);
					}
					nextgroups = finderState.getGroups();
					if (nextgroups.isEmpty()) {
						nextgroups = null;
					} else if (mode.findNonOverlapping()) {
						nextgroups = TreeSets.of(nextgroups.first());
					}
					return true;
				} else {
					groups = finderState.getGroups();
					if (groups.isEmpty()) {
						groups = null;
					} else {
						groups = TreeSets.of(groups.first());
					}
					nextgroups = null;
				}
			}
		}
		if (groups != null) {
			nextgroups = null;
			return true;
		} else {
			return false;
		}
	}
	
	private boolean longestMatchDetected(SortedSet<Groups> nextgroups) {
		if (groups == null || groups.isEmpty()) {
			return false;
		}
		Groups longest = groups.first();
		for (Groups nextgroup: nextgroups) {
			if (nextgroup.subsumes(longest)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public long start() {
		if (groups.isEmpty()) {
			return -1;
		}
		Groups longest = groups.first();
		return longest.getStart();
	}

	@Override
	public long end() {
		if (groups.isEmpty()) {
			return -1;
		}
		Groups longest = groups.first();
		return longest.getEnd();
	}

	@Override
	public String group() {
		if (groups.isEmpty()) {
			return null;
		}
		Groups longest = groups.first();
		return input.slice(longest.getStart(), longest.getEnd()).getString();
	}
	
	@Override
	public List<String> groups() {
		List<String> textgroups = new ArrayList<>(groups.size());
		for (Groups group : groups) {
			textgroups.add(input.slice(group.getStart(), group.getEnd()).getString());
		}
		return textgroups;
	}

}
