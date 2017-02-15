package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static net.amygdalum.util.text.CharUtils.after;

import java.util.Arrays;
import java.util.List;

import net.amygdalum.util.text.CharRange;

public final class BitMaskCharClassMapper implements CharClassMapper {

	private int dead;
	private char[] chars;
	private int[][] bytes;

	public BitMaskCharClassMapper(List<CharRange> liveRanges) {
		this.dead = deadClass(liveRanges);
		this.chars = chars(dead, liveRanges);
		this.bytes = computeBytes(dead, liveRanges);
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

	private static int[][] computeBytes(int dead, List<CharRange> liveRanges) {
		int[] deadbytes = new int[256];
		Arrays.fill(deadbytes, dead);

		int[][] bytes = new int[256][];
		Arrays.fill(bytes, deadbytes);

		int index = dead;

		for (CharRange range : liveRanges) {
			index++;

			int lowFrom = range.from & 0xff;
			int highFrom = (range.from >> 8) & 0xff;
			int lowTo = range.to & 0xff;
			int highTo = (range.to >> 8) & 0xff;

			for (int i = highFrom; i <= highTo; i++) {
				if (bytes[i] == deadbytes) {
					bytes[i] = new int[256];
					Arrays.fill(bytes[i], dead);
				}
				int start = (i == highFrom) ? lowFrom : 0;
				int end = (i == highTo) ? lowTo : 255;
				for (int j = start; j <= end; j++) {
					bytes[i][j] = index;
				}
			}
		}
		return bytes;
	}

	@Override
	public int getIndex(char ch) {
		int h = (ch >> 8) & 0xff;
		int l = ch & 0xff;
		return bytes[h][l];
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
