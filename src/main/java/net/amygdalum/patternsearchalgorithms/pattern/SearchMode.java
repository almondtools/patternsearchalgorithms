package net.amygdalum.patternsearchalgorithms.pattern;

public class SearchMode implements PatternOption {

	public static final SearchMode ALL = new SearchMode(false, true);
	public static final SearchMode LONGEST_NON_OVERLAPPING = new SearchMode(true, false);
	public static final SearchMode LONGEST_WITH_OVERLAP = new SearchMode(true, true);
	public static final SearchMode FIRSTMATCH_NON_OVERLAPPING = new SearchMode(false, false);
	public static final SearchMode DEFAULT = LONGEST_NON_OVERLAPPING;
	
	private boolean longest;
	private boolean overlapping;
	
	private SearchMode(boolean longest, boolean overlapping) {
		this.longest = longest;
		this.overlapping = overlapping;
	}
	
	public boolean findLongest() {
		return longest;
	}

	public boolean findAll() {
		return !longest;
	} 

	public boolean findOverlapping() {
		return overlapping;
	}
	
	public boolean findNonOverlapping() {
		return !overlapping;
	}
	
	public static SearchMode firstOf(PatternOption[] options) {
		for (PatternOption option : options) {
			if (option instanceof SearchMode) {
				return (SearchMode) option;
			}
		}
		return DEFAULT;
	}

}
