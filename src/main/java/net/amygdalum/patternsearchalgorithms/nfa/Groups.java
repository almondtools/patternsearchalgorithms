package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import net.amygdalum.util.io.ByteProvider;

public class Groups implements Comparable<Groups> {

	private static final SortedSet<SubMatch> EMPTY = new TreeSet<>();

	private long start;
	private long end;
	private SubMatch[] submatches;
	private SortedSet<SubMatch> allsubmatches;

	public Groups() {
		this.start = -1;
		this.end = -1;
		this.allsubmatches = EMPTY;
	}

	public Groups(long start, long end) {
		this.start = start;
		this.end = end;
		this.allsubmatches = EMPTY;
	}

	public Groups(long start, long end, SubMatch[] submatches, SortedSet<SubMatch> allsubmatches) {
		this.start = start;
		this.end = end;
		this.submatches = submatches;
		this.allsubmatches = allsubmatches;
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

	public boolean isComplete() {
		return submatches != null;
	}

	public boolean invalid() {
		return start == -1
			|| end == -1;
	}

	public boolean valid() {
		return start > -1
			&& end > -1;
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

	public long getStart(int no) {
		if (submatches != null && no < submatches.length) {
			SubMatch subMatch = submatches[no];
			if (subMatch != null) {
				return subMatch.start;
			}
		}
		return -1;
	}

	public long getEnd() {
		return end;
	}

	public long getEnd(int no) {
		if (submatches != null && no < submatches.length) {
			SubMatch subMatch = submatches[no];
			if (subMatch != null) {
				return subMatch.end;
			}
		}
		return -1;
	}

	public long range() {
		return end - start;
	}

	public Groups startGroup(int no, long pos) {
		if (no == 0) {
			return new Groups(pos, end, submatches, allsubmatches);
		} else {
			SubMatch[] newSubmatches = newSubmatches(no);
			newSubmatches[no] = SubMatch.start(pos);
			return new Groups(start, end, newSubmatches, allsubmatches);
		}
	}

	public Groups endGroup(int no, long pos) {
		if (no == 0) {
			return new Groups(start, pos, submatches, allsubmatches);
		} else {
			SubMatch[] newSubmatches = newSubmatches(no);
			SortedSet<SubMatch> newAllSubmatches = new TreeSet<>(allsubmatches);
			if (newSubmatches[no] != null) {
				SubMatch newSubmatch = newSubmatches[no].end(pos);
				newSubmatches[no] = newSubmatch;
				newAllSubmatches.add(newSubmatch);
			}
			return new Groups(start, end, newSubmatches, newAllSubmatches);
		}
	}

	public SubMatch[] newSubmatches(int no) {
		int len = submatches == null ? 0 : submatches.length;
		if (len <= no) {
			len = no + 1;
		}
		SubMatch[] newsubmatches = new SubMatch[len];
		if (submatches != null) {
			System.arraycopy(submatches, 0, newsubmatches, 0, submatches.length);
		}
		return newsubmatches;
	}

	public void reset() {
		this.start = -1;
		this.end = -1;
	}

	public void update(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public void update(Groups group) {
		submatches = group.submatches;
		allsubmatches = group.allsubmatches;
	}

	@Override
	public String toString() {
		return start + ":" + end + (submatches == null ? "" : " " + Arrays.toString(submatches));
	}

	@Override
	public int compareTo(Groups group) {
		int compare = Long.compare(start, group.start);
		if (compare == 0) {
			compare = Long.compare(end, group.end);
		}
		if (compare == 0) {
			compare = compareSubMatches(allsubmatches, group.allsubmatches);
		}
		return compare;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(start).hashCode() * 7 + Long.valueOf(end).hashCode() * 5 + allsubmatches.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Groups that = (Groups) obj;
		return this.start == that.start
			&& this.end == that.end
			&& Arrays.equals(this.submatches, that.submatches)
			&& this.allsubmatches.equals(that.allsubmatches);
	}

	public void process(ByteProvider input, NFA grouper) {
		long groupStart = start;
		NFAMatcherState state = NFAMatcherState.of(grouper.getStart(), new Groups(), groupStart);
		input.move(groupStart);
		while (!input.finished() && input.current() < getEnd()) {
			byte b = input.next();
			long current = input.current();
			state = state.next(b, current);
		}
		update(state.getGroups().first());
	}

	private static int compareSubMatches(SortedSet<SubMatch> submatches1, SortedSet<SubMatch> submatches2) {
		Queue<SubMatch> sub1 = new PriorityQueue<>(submatches1);
		Queue<SubMatch> sub2 = new PriorityQueue<>(submatches2);
		while (!sub1.isEmpty() && !sub2.isEmpty()) {
			SubMatch match1 = sub1.remove();
			SubMatch match2 = sub2.remove();
			int compare = match2.compareTo(match1);
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	private static class SubMatch implements Comparable<SubMatch> {

		public long start;
		public long end;

		private SubMatch(long start) {
			this.start = start;
		}

		private SubMatch(long start, long end) {
			this.start = start;
			this.end = end;
		}

		public static SubMatch start(long start) {
			return new SubMatch(start);
		}

		public SubMatch end(long end) {
			return new SubMatch(start, end);
		}

		@Override
		public String toString() {
			return start + ":" + end;
		}

		@Override
		public int compareTo(SubMatch submatch) {
			int compare = Long.compare(start, submatch.start);
			if (compare == 0) {
				compare = Long.compare(end, submatch.end);
			}
			return compare;
		}

		@Override
		public int hashCode() {
			return Long.valueOf(start).hashCode() * 17 + Long.valueOf(end).hashCode() * 13;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SubMatch that = (SubMatch) obj;
			return this.start == that.start
				&& this.end == that.end;
		}

	}

}
