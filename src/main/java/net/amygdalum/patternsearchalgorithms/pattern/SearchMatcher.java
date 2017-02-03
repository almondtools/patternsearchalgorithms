package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.amygdalum.patternsearchalgorithms.dfa.DFA;
import net.amygdalum.patternsearchalgorithms.nfa.Groups;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.util.builders.TreeSets;
import net.amygdalum.util.io.ByteProvider;
import net.amygdalum.util.io.ReverseByteProvider;

public class SearchMatcher implements Matcher {

	private SearchMode mode;
	private DFA finder;
	private DFA backmatcher;
	private NFA grouper;
	private ByteProvider input;
	private long start;

	private int backmatcherState;
	private int finderState;
	private SortedSet<Groups> groups;

	public SearchMatcher(SearchMode mode, DFA finder, DFA backmatcher, NFA grouper, ByteProvider input) {
		this.mode = mode;
		this.finder = finder;
		this.backmatcher = backmatcher;
		this.grouper = grouper;
		this.input = input;
		this.start = input.current();
		this.backmatcherState = -2;
		this.finderState = -2;
	}

	@Override
	public boolean matches() {
		ByteProvider reverseInput = new ReverseByteProvider(input);
		if (backmatcherState == -2) {
			backmatcherState = backmatcher.start;
			input.finish();
		}
		long end = input.current();
		while (!reverseInput.finished() && backmatcherState >= 0) {
			byte b = reverseInput.next();
			backmatcherState = backmatcher.next(backmatcherState, b);
		}
		if (backmatcher.accept(backmatcherState)) {
			groups = TreeSets.of(new Groups(0, end));
			return true;
		}
		return false;
	}

	@Override
	public boolean prefixes() {
		ByteProvider reverseInput = new ReverseByteProvider(input);
		SortedSet<Groups> groups = new TreeSet<>();
		if (finderState == -2) {
			finderState = finder.start;
		}
		while (!input.finished() && finderState >= 0) {
			byte b = input.next();
			finderState = finder.next(finderState, b);
			if (finder.accept(finderState)) {
				Groups nextgroup = verifyPrefix(reverseInput);
				if (nextgroup != null) {
					if (mode.findLongest()) {
						Groups shortestSubsumed = new Groups(nextgroup.getStart(), nextgroup.getStart());
						SortedSet<Groups> subsumed = groups.subSet(shortestSubsumed, nextgroup);
						groups.removeAll(subsumed);
					}
					groups.add(nextgroup);
				}
			}
		}
		if (finder.accept(finderState)) {
			Groups nextgroup = verifyPrefix(reverseInput);
			if (nextgroup != null) {
				if (mode.findLongest()) {
					Groups shortestSubsumed = new Groups(nextgroup.getStart(), nextgroup.getStart());
					SortedSet<Groups> subsumed = groups.subSet(shortestSubsumed, nextgroup);
					groups.removeAll(subsumed);
				}
				groups.add(nextgroup);
			}
		}
		if (groups.isEmpty()) {
			return false;
		} else {
			this.groups = groups;
			return true;
		}
	}

	private Groups verifyPrefix(ByteProvider reverseInput) {
		long reset = reverseInput.current();
		backmatcherState = backmatcher.start;
		while (!reverseInput.finished() && backmatcherState >= 0) {
			byte b = reverseInput.next();
			backmatcherState = backmatcher.next(backmatcherState, b);
		}
		input.move(reset);
		if (backmatcher.accept(backmatcherState)) {
			return new Groups(0, reset);
		} else {
			return null;
		}
	}

	@Override
	public boolean find() {
		ByteProvider reverseInput = new ReverseByteProvider(input);
		SortedSet<Groups> groups = new TreeSet<>();
		if (finderState == -2) {
			finderState = finder.start;
		}
		while (!input.finished() && finderState >= 0) {
			byte b = input.next();
			finderState = finder.next(finderState, b);
			if (finder.accept(finderState)) {
				SortedSet<Groups> nextgroups = verifyMatch(reverseInput);
				if (mode.findLongest()) {
					for (Groups nextgroup : nextgroups) {
						Groups shortestSubsumed = new Groups(nextgroup.getStart(), nextgroup.getStart());
						SortedSet<Groups> subsumed = groups.subSet(shortestSubsumed, nextgroup);
						groups.removeAll(subsumed);
					}
					groups.addAll(nextgroups);
				}
			}
		}
		if (finder.accept(finderState)) {
			SortedSet<Groups> nextgroups = verifyMatch(reverseInput);
			if (mode.findLongest()) {
				for (Groups nextgroup : nextgroups) {
					Groups shortestSubsumed = new Groups(nextgroup.getStart(), nextgroup.getStart());
					SortedSet<Groups> subsumed = groups.subSet(shortestSubsumed, nextgroup);
					groups.removeAll(subsumed);
				}
				groups.addAll(nextgroups);
			}
		}
		if (groups.isEmpty()) {
			return false;
		} else {
			this.groups = groups;
			return true;
		}
	}

	private SortedSet<Groups> verifyMatch(ByteProvider reverseInput) {
		long reset = reverseInput.current();
		SortedSet<Groups> matches = new TreeSet<>();
		backmatcherState = backmatcher.start;
		while (!reverseInput.finished() && backmatcherState >= 0) {
			byte b = reverseInput.next();
			backmatcherState = backmatcher.next(backmatcherState, b);
			if (backmatcher.accept(backmatcherState)) {
				matches.add(new Groups(reverseInput.current(), reset));
			}
		}
		if (backmatcher.accept(backmatcherState)) {
			matches.add(new Groups(reverseInput.current(), reset));
		}
		input.move(reset);
		return matches;
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
