package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static net.amygdalum.util.text.CharUtils.after;

import java.util.List;

import net.amygdalum.util.text.CharRange;

public class SmallRangeCharClassMapper implements CharClassMapper {

	private char[] chars;
	private char lowerBound;
	private char upperBound;
	private int dead;
	private int[] charToClass;

	public SmallRangeCharClassMapper(List<CharRange> liveRanges) {
		this.lowerBound = liveRanges.isEmpty() ? MAX_VALUE : liveRanges.get(0).from;
		this.upperBound = liveRanges.isEmpty() ? MIN_VALUE : liveRanges.get(liveRanges.size() - 1).to;
		this.dead = deadClass(liveRanges);
		this.chars = chars(dead, liveRanges);
		this.charToClass = computeCharClasses(dead, liveRanges);
	}

	private static int deadClass(List<CharRange> liveRanges) {
		if (liveRanges.isEmpty()) {
			return 0;
		}
		char nextCandidate = MIN_VALUE;
		for (CharRange range : liveRanges) {
			if (range.contains(nextCandidate)) {
				nextCandidate = after(range.to);
			} else {
				return 0;
			}
		}
		if (nextCandidate == after(MAX_VALUE)) {
			return -1;
		} else {
			return 0;
		}
	}

	private static char[] chars(int dead, List<CharRange> liveRanges) {
		int liveIndex = dead + 1;
		char[] chars = new char[liveIndex + liveRanges.size()];
		if (dead == 0)  {
			chars[dead] = deadChar(liveRanges);
		}
		for (CharRange range : liveRanges) {
			chars[liveIndex] = range.from;
			liveIndex++;
		}
		return chars;
	}

	private static char deadChar(List<CharRange> liveRanges) {
		char nextCandidate = MIN_VALUE;
		for (CharRange range : liveRanges) {
			if (range.contains(nextCandidate)) {
				nextCandidate = after(range.to);
			} else {
				return nextCandidate;
			}
		}
		if (nextCandidate == after(MAX_VALUE)) {
			return 0;
		} else {
			return nextCandidate;
		}
	}

	private static int[] computeCharClasses(int dead, List<CharRange> liveRanges) {
		if (liveRanges.isEmpty()) {
			return new int[0];
		}
		char low = liveRanges.get(0).from;
		char high = liveRanges.get(liveRanges.size() - 1).to;
		int[] charToClass = new int[high - low + 1];
		int charClass = dead + 1;
		for (CharRange range : liveRanges) {
			for (int i = range.from; i <= range.to; i++) {
				charToClass[i - low] = charClass;
			}
			charClass++;
		}
		return charToClass;
	}

	@Override
	public int getIndex(char ch) {
		if (ch < lowerBound || ch > upperBound) {
			return dead;
		}
		return charToClass[ch - lowerBound];
	}

	@Override
	public int indexCount() {
		return chars.length;
	}

	@Override
	public char representative(int i) {
		return chars[i];
	}

	@Override
	public String representatives(List<Integer> indexes) {
		if (indexes == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		for (Integer i : indexes) {
			buffer.append(chars[i]);
		}
		return buffer.toString();
	}

}
