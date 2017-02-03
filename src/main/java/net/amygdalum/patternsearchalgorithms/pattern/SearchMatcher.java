package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import net.amygdalum.patternsearchalgorithms.dfa.DFA;
import net.amygdalum.patternsearchalgorithms.nfa.Groups;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.NFAMatcherState;
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
	private NFAMatcherState grouperState;
	private long backmatchTo;
	private NavigableSet<Groups> groups;
	private NavigableSet<Groups> nextgroups;

	public SearchMatcher(SearchMode mode, DFA finder, DFA backmatcher, NFA grouper, ByteProvider input) {
		this.mode = mode;
		this.finder = finder;
		this.backmatcher = backmatcher;
		this.grouper = grouper;
		this.input = input;
		this.start = input.current();
		this.backmatcherState = -2;
		this.finderState = -2;
		this.grouperState = null;
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
			groups = TreeSets.of(new Groups(start, end));
			return true;
		}
		return false;
	}

	@Override
	public boolean prefixes() {
		ByteProvider reverseInput = new ReverseByteProvider(input);
		NavigableSet<Groups> groups = new TreeSet<>();
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
						NavigableSet<Groups> subsumed = groups.subSet(shortestSubsumed, true, nextgroup, false);
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
					NavigableSet<Groups> subsumed = groups.subSet(shortestSubsumed, true, nextgroup, false);
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
		while (reverseInput.current() > start && backmatcherState >= 0) {
			byte b = reverseInput.next();
			backmatcherState = backmatcher.next(backmatcherState, b);
		}
		input.move(reset);
		if (backmatcher.accept(backmatcherState)) {
			return new Groups(start, reset);
		} else {
			return null;
		}
	}

	@Override
	public boolean find() {
		ByteProvider reverseInput = new ReverseByteProvider(input);
		NavigableSet<Groups> groups = new TreeSet<>();
		if (nextgroups != null) {
			groups.addAll(nextgroups);
			nextgroups = null;
		}
		if (finderState == -2) {
			finderState = finder.start;
			backmatchTo = input.current();
			if (finder.accept(finderState)) {
				groups = TreeSets.of(new Groups(backmatchTo, backmatchTo));
				if (mode.findAll()) {
					this.groups = groups;
					return true;
				} else {
					nextgroups = null;
				}
			}
		}
		while (!input.finished() && finderState >= 0) {
			byte b = input.next();
			finderState = finder.next(finderState, b);
			if (finder.accept(finderState)) {
				NavigableSet<Groups> nextgroups = verifyMatch(reverseInput);
				if (mode.findLongest()) {
					for (Groups nextgroup : nextgroups) {
						Groups shortestSubsumed = new Groups(nextgroup.getStart(), nextgroup.getStart());
						NavigableSet<Groups> subsumed = groups.subSet(shortestSubsumed, true, nextgroup, false);
						groups.removeAll(subsumed);
					}
				}
				groups.addAll(nextgroups);
				if (mode.findAll()) {
					this.groups = groups;
					if (mode.findNonOverlapping()) {
						finderState = -2;
					}
					return true;
				}
			}
			if (!groups.isEmpty() && finder.silent(finderState)) {
				break;
			}
		}
		if (finder.accept(finderState)) {
			NavigableSet<Groups> nextgroups = verifyMatch(reverseInput);
			if (mode.findLongest()) {
				for (Groups nextgroup : nextgroups) {
					Groups shortestSubsumed = new Groups(nextgroup.getStart(), nextgroup.getStart());
					NavigableSet<Groups> subsumed = groups.subSet(shortestSubsumed, true, nextgroup, false);
					groups.removeAll(subsumed);
				}
				groups.addAll(nextgroups);
				if (mode.findAll()) {
					this.groups = groups;
					if (mode.findNonOverlapping()) {
						finderState = -2;
					}
					return true;
				}
			}
		}
		if (groups.isEmpty()) {
			return false;
		} else {
			Groups group = groups.pollFirst();
			Iterator<Groups> groupIterator = groups.iterator();
			while (groupIterator.hasNext()) {
				Groups nextGroup = groupIterator.next();
				if (group.subsumes(nextGroup)) {
					groupIterator.remove();
				} else if (mode.findNonOverlapping() && group.overlaps(nextGroup)) {
					groupIterator.remove();
				}
			}
			this.groups = TreeSets.of(group);
			this.nextgroups = groups;
			if (mode.findNonOverlapping()) {
				backmatchTo = group.getEnd();
			}
			return true;
		}
	}

	private NavigableSet<Groups> verifyMatch(ByteProvider reverseInput) {
		long reset = reverseInput.current();
		NavigableSet<Groups> matches = new TreeSet<>();
		backmatcherState = backmatcher.start;
		while (reverseInput.current() > backmatchTo && backmatcherState >= 0) {
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

	private void process(Groups groups) {
		long groupStart = groups.getStart();
		grouperState = NFAMatcherState.of(grouper.getStart(), new Groups(), groupStart);
		input.move(groupStart);
		while (!input.finished() && input.current() < groups.getEnd()) {
			byte b = input.next();
			long current = input.current();
			grouperState = grouperState.next(b, current);
		}
		groups.update(grouperState.getGroups().first());
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
