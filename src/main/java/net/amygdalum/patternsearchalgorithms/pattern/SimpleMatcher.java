package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

	public SimpleMatcher(SearchMode mode, DFA matcher, NFA finder, ByteProvider input) {
		this.mode = mode;
		this.matcher = matcher;
		this.finder = finder;
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
			long current = input.current();
			finderState = NFAMatcherState.of(finder.getStart(), new Groups(), current);
			if (finderState.isAccepting(current)) {
				if (mode.findAll()) {
					groups = finderState.getGroups();
					nextgroups = null;
					return true;
				} else {
					groups = finderState.getGroups();
					if (groups.isEmpty()) {
						groups = null;
					} else {
						groups = longestLeftMost(groups);
					}
					nextgroups = null;
				}
			}
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
						nextgroups = longestLeftMost(nextgroups);
					}
					return true;
				} else {
					groups = finderState.getGroups();
					if (groups.isEmpty()) {
						groups = null;
					} else {
						groups = longestLeftMost(groups);
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

	private SortedSet<Groups> longestLeftMost(SortedSet<Groups> groups) {
		SortedSet<Groups> longestLeftmost = new TreeSet<>();
		Groups last = null;
		for (Groups group : groups) {
			if (last == null) {
				last = group;
				longestLeftmost.add(group);
			} else if (group.subsumes(last) && last.subsumes(group)) {
				longestLeftmost.add(group);
			} else {
				break;
			}
		}
		return longestLeftmost;
	}

	private boolean longestMatchDetected(SortedSet<Groups> nextgroups) {
		if (groups == null || groups.isEmpty()) {
			return false;
		}
		Groups longest = groups.first();
		for (Groups nextgroup : nextgroups) {
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
	public long start(int no) {
		if (groups.isEmpty()) {
			return -1;
		}
		Groups longest = groups.first();
		if (!longest.isComplete()) {
			process(longest);
		}
		return longest.getStart(no);
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
	public long end(int no) {
		if (groups.isEmpty()) {
			return -1;
		}
		Groups longest = groups.first();
		if (!longest.isComplete()) {
			process(longest);
		}
		return longest.getEnd(no);
	}

	private void process(Groups groups) {
		long groupStart = groups.getStart();
		finderState = NFAMatcherState.of(finder.getStart(), new Groups(), groupStart);
		input.move(groupStart);
		while (!input.finished() && input.current() < groups.getEnd()) {
			byte b = input.next();
			long current = input.current();
			finderState = finderState.next(b, current);
		}
		groups.update(finderState.getGroups().first());
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
	public String group(int no) {
		if (groups.isEmpty()) {
			return null;
		}
		Groups longest = groups.first();
		if (!longest.isComplete()) {
			process(longest);
		}
		long start = longest.getStart(no);
		long end = longest.getEnd(no);
		if (start != -1 && end != -1 && start <= end) {
			return input.slice(start, end).getString();
		} else {
			return null;
		}
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
