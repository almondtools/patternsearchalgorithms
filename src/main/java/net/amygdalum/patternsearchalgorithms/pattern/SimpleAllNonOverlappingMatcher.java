package net.amygdalum.patternsearchalgorithms.pattern;

import net.amygdalum.patternsearchalgorithms.dfa.DFA;
import net.amygdalum.patternsearchalgorithms.nfa.Groups;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.util.io.ByteProvider;

public class SimpleAllNonOverlappingMatcher implements Matcher {

	private DFA matcher;
	private NFA grouper;
	private ByteProvider input;
	private final long start;

	private Groups groups;
	private int nextstate;

	public SimpleAllNonOverlappingMatcher(DFA matcher, NFA grouper, ByteProvider input) {
		this.matcher = matcher;
		this.grouper = grouper;
		this.input = input;
		this.start = input.current();
		this.groups = new Groups();
		this.nextstate = -1;
	}

	@Override
	public boolean matches() {
		int state = matcher.start;
		while (!input.finished() && state >= 0) {
			byte b = input.next();
			state = matcher.next(state, b);
		}
		if (matcher.accept(state)) {
			groups.update(start, input.current());
			input.move(start);
			return true;
		}
		input.move(start);
		return false;
	}

	@Override
	public boolean prefixes() {
		int state = matcher.start;
		if (matcher.accept(state)) {
			groups.update(start, input.current());
			input.move(start);
			return true;
		}
		while (!input.finished() && state >= 0) {
			byte b = input.next();
			state = matcher.next(state, b);
			if (matcher.accept(state)) {
				groups.update(start, input.current());
				input.move(start);
				return true;
			}
		}
		if (matcher.accept(state)) {
			groups.update(start, input.current());
			input.move(start);
			return true;
		}
		input.move(start);
		return false;
	}

	@Override
	public boolean find() {
		int state = matcher.start;
		long localstart = input.current();
		if (nextstate >= 0) {
			groups.reset();
			state = nextstate;
			nextstate = -1;
		} else if (matcher.accept(state)) {
			groups.update(start, input.current());
			nextstate = state;
			input.move(groups.getEnd());
			return true;
		} else {
			groups.reset();
		}
		while (!input.finished() && state >= 0) {
			byte b = input.next();
			state = matcher.next(state, b);
			if (matcher.accept(state)) {
				groups.update(localstart, input.current());
				input.move(groups.getEnd());
				return true;
			} else if (state == -1) {
				localstart = localstart + 1;
				input.move(localstart);
				state = matcher.start;
				if (matcher.accept(state)) {
					groups.update(localstart, localstart);
					nextstate = state;
					return true;
				}
				continue;
			}
		}
		if (matcher.accept(state)) {
			groups.update(localstart, input.current());
			return true;
		}
		return false;
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
			return input.slice(start, end).getString();
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
			return input.slice(start, end).getString();
		} else {
			return null;
		}
	}

}
