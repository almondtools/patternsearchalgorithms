package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.List;

public class Groups implements Comparable<Groups> {

	private long start;
	private long end;

	public Groups() {
		this.start = -1;
		this.end = -1;
	}

	public Groups(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public static Groups longest(List<Groups> groups) {
		Groups longest = null;
		for (Groups group : groups) {
			if (longest == null || longest.range() < group.range()) {
				longest = group;
			}
		}
		return longest;
	}

	public boolean invalid() {
		return start == -1
			|| end == -1;
	}

	public boolean subsumes(Groups group) {
		return start <= group.start && end >= group.end;
	}

	public boolean overlaps(Groups group) {
		return start <= group.start && end >= group.start
			|| start <= group.end && end >= group.end;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}
	
	public long range() {
		return end - start;
	}

	public Groups startGroup(int no, long pos) {
		if (no == 0) {
			return new Groups(pos, this.end);
		}
		return this;
	}

	public Groups endGroup(int no, long pos) {
		if (no == 0) {
			return new Groups(this.start, pos);
		}
		return this;
	}

	@Override
	public String toString() {
		return start + ":" + end;
	}

	@Override
	public int compareTo(Groups group) {
		int compare = Long.compare(start, group.start);
		if (compare == 0) {
			compare = Long.compare(end, group.end);
		}
		return compare;
	}

}
