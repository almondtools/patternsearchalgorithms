package net.amygdalum.patternsearchalgorithms.pattern.chars;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import net.amygdalum.patternsearchalgorithms.automaton.chars.DFA;
import net.amygdalum.patternsearchalgorithms.automaton.chars.Groups;
import net.amygdalum.patternsearchalgorithms.automaton.chars.NFA;
import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.io.ReverseCharProvider;

public class SearchAllNonOverlappingMatcher implements Matcher {

	private DFA finder;
	private DFA backmatcher;
	private NFA grouper;
	private CharProvider input;
	private final long start;

	private Groups groups;
	private Queue<Groups> nextgroups;

	public SearchAllNonOverlappingMatcher(DFA finder, DFA backmatcher, NFA grouper, CharProvider input) {
		this.finder = finder;
		this.backmatcher = backmatcher;
		this.grouper = grouper;
		this.input = input;
		this.start = input.current();
		this.groups = new Groups();
		this.nextgroups = new PriorityQueue<>();
	}

	@Override
	public boolean matches() {
		input.finish();
		long end = input.current();
		boolean match = verifyPrefix(start);
		input.move(start);
		if (match) {
			groups.update(start, end);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean prefixes() {
		int state = finder.start;
		if (finder.accept(state)) {
			groups.update(start, start);
			input.move(start);
			return true;
		}
		while (!input.finished() && state >= 0 && !finder.silent(state)) {
			char c = input.next();
			state = finder.next(state, c);
			if (finder.accept(state)) {
				long end = input.current();
				boolean match = verifyPrefix(start);
				input.move(end);
				if (match) {
					groups.update(start, end);
					input.move(start);
					return true;
				}
			}
		}
		input.move(start);
		return false;
	}

	private boolean verifyPrefix(long pos) {
		CharProvider reverse = new ReverseCharProvider(input);
		int state = backmatcher.start;
		while (reverse.current() > pos && state >= 0) {
			char c = reverse.next();
			state = backmatcher.next(state, c);
		}
		return backmatcher.accept(state);
	}

	@Override
	public boolean find() {
		if (!nextgroups.isEmpty()) {
			Groups first = nextGroup();
			groups.update(first.getStart(), first.getEnd());
			return true;
		}
		int state = finder.start;
		if (finder.accept(state)) {
			nextgroups.add(new Groups(start, input.current()));
		} else {
			groups.reset();
		}
		while (!input.finished() && state >= 0 && !finder.silent(state)) {
			char c = input.next();
			state = finder.next(state, c);
			if (finder.accept(state)) {
				long end = input.current();
				verifyMatches();
				input.move(end);
			}
		}
		if (finder.accept(state)) {
			long end = input.current();
			verifyMatches();
			input.move(end);
		}
		if (nextgroups.isEmpty()) {
			return false;
		} else {
			Groups first = nextGroup();
			groups.update(first.getStart(), first.getEnd());
			return true;
		}
	}

	private void verifyMatches() {
		long end = input.current();
		
		CharProvider reverse = new ReverseCharProvider(input);
		int state = backmatcher.start;
		if (backmatcher.accept(state)) {
			nextgroups.add(new Groups(end, end));
		}
		while (!reverse.finished() && state >= 0) {
			char c = reverse.next();
			state = backmatcher.next(state, c);
			if (backmatcher.accept(state)) {
				long start = input.current();
				nextgroups.add(new Groups(start, end));
			}
		}
		if (backmatcher.accept(state)) {
			long start = input.current();
			nextgroups.add(new Groups(start, end));
		}
	}

	private Groups nextGroup() {
		Groups groups = nextgroups.remove();
		Iterator<Groups> groupsIterator = nextgroups.iterator();
		while (groupsIterator.hasNext()) {
			Groups current = groupsIterator.next();
			if (current.overlaps(groups) && groups.getEnd() != current.getStart()) {
				groupsIterator.remove();
			} else {
				break;
			}
		}
		
		return groups;
	}

	@Override
	public long start() {
		return groups.getStart();
	}

	@Override
	public long start(int no) {
		if (!groups.isComplete()) {
			groups.process(input, grouper);
		}
		return groups.getStart(no);
	}

	@Override
	public long end() {
		return groups.getEnd();
	}

	@Override
	public long end(int no) {
		if (!groups.isComplete()) {
			groups.process(input, grouper);
		}
		return groups.getEnd(no);
	}

	@Override
	public String group() {
		long start = groups.getStart();
		long end = groups.getEnd();
		if (start != -1 && end != -1 && start <= end) {
			return input.slice(start, end);
		} else {
			return null;
		}
	}

	@Override
	public String group(int no) {
		if (!groups.isComplete()) {
			groups.process(input, grouper);
		}
		long start = groups.getStart(no);
		long end = groups.getEnd(no);
		if (start != -1 && end != -1 && start <= end) {
			return input.slice(start, end);
		} else {
			return null;
		}
	}

}
