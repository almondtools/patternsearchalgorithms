package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static net.amygdalum.util.text.CharUtils.after;

import java.util.Arrays;
import java.util.List;

import net.amygdalum.util.text.CharRange;

public final class LowByteCharClassMapper implements CharClassMapper {

	private int dead;
	private char[] chars;
	private int[] lowbyte;

	public LowByteCharClassMapper(List<CharRange> liveRanges) {
		this.dead = deadClass(liveRanges);
		this.chars = chars(dead, liveRanges);
		this.lowbyte = computeLowByte(dead, liveRanges);
	}

	private static char[] chars(int dead, List<CharRange> liveRanges) {
		int liveIndex = dead + 1;
		char[] chars = new char[liveIndex + liveRanges.size()];
		if (dead == 0) {
			chars[dead] = deadChar(liveRanges);
		}
		for (CharRange range : liveRanges) {
			chars[liveIndex] = range.from;
			liveIndex++;
		}
		return chars;
	}

	private static int deadClass(List<CharRange> liveRanges) {
		if (liveRanges.isEmpty()) {
			return 0;
		}
		char nextCandidate = (char) (liveRanges.get(0).from & 0xff00);
		char lastCandidate = (char) (liveRanges.get(0).from | 0x00ff);
		for (CharRange range : liveRanges) {
			if (range.contains(nextCandidate)) {
				nextCandidate = after(range.to);
			} else {
				return 0;
			}
		}
		if (nextCandidate > lastCandidate) {
			return -1;
		} else {
			return 0;
		}
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

	private static int[] computeLowByte(int dead, List<CharRange> liveRanges) {
		int[] lowbytes = new int[256];
		Arrays.fill(lowbytes, dead);
		int index = dead + 1;
		for (CharRange range : liveRanges) {
			for (int c = range.from; c <= range.to; c++) {
				int low = c & 0xff;
				lowbytes[low] = index;
			}
			index++;
		}
		return lowbytes;
	}

	@Override
	public int getIndex(char ch) {
		int l = ch & 0xff;
		return lowbyte[l];
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
