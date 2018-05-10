package net.amygdalum.patternsearchalgorithms.pattern;

import static net.amygdalum.patternsearchalgorithms.pattern.OptimizationTarget.MATCH;
import static net.amygdalum.patternsearchalgorithms.pattern.OptimizationTarget.SEARCH;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.ALL;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.FIRSTMATCH_NON_OVERLAPPING;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.LONGEST_NON_OVERLAPPING;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.LONGEST_WITH_OVERLAP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.patternsearchalgorithms.pattern.PatternRule.Charsets;
import net.amygdalum.patternsearchalgorithms.pattern.PatternRule.Only;

@Only({ MATCH, SEARCH })
@Charsets({"UTF-8", "CHARS"})
public class PatternPrefixTest {

	@Rule
	public PatternRule patterns = new PatternRule();

	@Test
	public void testPrefixAllIdempotent() throws Exception {
		Pattern pattern = patterns.compile("a+", ALL);
		Matcher matcher = pattern.matcher("axx");
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
	}

	@Test
	public void testPrefixLongestNonOverlappingIdempotent() throws Exception {
		Pattern pattern = patterns.compile("a+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("axx");
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
	}

	@Test
	public void testPrefixLongestOverlappingIdempotent() throws Exception {
		Pattern pattern = patterns.compile("a+", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("axx");
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
	}

	@Test
	public void testPrefixFirstNonOverlappingIdempotent() throws Exception {
		Pattern pattern = patterns.compile("a+", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("axx");
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
		assertTrue(matcher.prefixes());
	}

	@Test
	public void testPrefixPattern1fullMatch() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("abbc");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("abbc"));
	}

	@Test
	public void testPrefixPattern1partMatch() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("abbccc");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("abbc"));
	}

	@Test
	public void testPrefixPattern1noMatch() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("cabbccc");
		boolean success = matcher.prefixes();
		assertFalse(success);
	}

	@Test
	public void testPrefixPattern3all() throws Exception {
		Pattern pattern = patterns.compile("a*", ALL);
		Matcher matcher = pattern.matcher("aaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(0l));
		assertThat(matcher.group(), equalTo(""));
	}

	@Test
	public void testPrefixPattern3firstNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("a*", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("aaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(0l));
		assertThat(matcher.group(), equalTo(""));
	}

	@Test
	public void testPrefixPattern3longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("a*", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("aaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("aaaa"));
	}

	@Test
	public void testPrefixPattern3longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("a*", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("aaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("aaaa"));
	}

	@Test
	public void testPrefixPattern4simple() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+");
		Matcher matcher = pattern.matcher("axxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testPrefixPattern4longest() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("abaxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(3l));
		assertThat(matcher.group(), equalTo("aba"));
	}

	@Test
	public void testPrefixPattern4first() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("abaxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testPrefixPattern5all() throws Exception {
		Pattern pattern = patterns.compile("cabc|ab");
		Matcher matcher = pattern.matcher("cabcxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("cabc"));
	}

	@Test
	public void testPrefixPattern6all() throws Exception {
		Pattern pattern = patterns.compile("ba*", ALL);
		Matcher matcher = pattern.matcher("baaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo("b"));
	}

	@Test
	public void testPrefixPattern6firstNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ba*", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("baaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo("b"));
	}

	@Test
	public void testPrefixPattern6longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ba*", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("baaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(5l));
		assertThat(matcher.group(), equalTo("baaaa"));
	}

	@Test
	public void testPrefixPattern6longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ba*", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("baaaaxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(5l));
		assertThat(matcher.group(), equalTo("baaaa"));
	}

	@Test
	public void testPrefixSubmatchPattern6() throws Exception {
		Pattern pattern = patterns.compile("([a-e]|(abc))+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("cabcdecabcxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("cabcdecabc"));
		assertThat(matcher.start(1), equalTo(7l));
		assertThat(matcher.end(1), equalTo(10l));
		assertThat(matcher.group(1), equalTo("abc"));
		assertThat(matcher.start(2), equalTo(7l));
		assertThat(matcher.end(2), equalTo(10l));
		assertThat(matcher.group(2), equalTo("abc"));
	}
	
	@Test
	public void testMatchPattern9() throws Exception {
		Pattern pattern = patterns.compile("And God ([A-Za-z]+ |.){0,2}take them away");
		assertTrue(pattern.matcher("And God take them away").matches());
		assertTrue(pattern.matcher("And God should take them away").matches());
		assertTrue(pattern.matcher("And God should not take them away").matches());
		assertTrue(pattern.matcher("And God ..take them away").matches());
		assertFalse(pattern.matcher("And God ...take them away").matches());
	}



}