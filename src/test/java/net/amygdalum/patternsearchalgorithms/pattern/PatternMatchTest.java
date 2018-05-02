package net.amygdalum.patternsearchalgorithms.pattern;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static net.amygdalum.patternsearchalgorithms.pattern.OptimizationTarget.MATCH;
import static net.amygdalum.patternsearchalgorithms.pattern.OptimizationTarget.SEARCH;
import static net.amygdalum.patternsearchalgorithms.pattern.RegexOption.DOT_ALL;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.ALL;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.FIRSTMATCH_NON_OVERLAPPING;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.LONGEST_NON_OVERLAPPING;
import static net.amygdalum.patternsearchalgorithms.pattern.SearchMode.LONGEST_WITH_OVERLAP;
import static net.amygdalum.util.text.CharUtils.after;
import static net.amygdalum.util.text.CharUtils.before;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.patternsearchalgorithms.pattern.PatternRule.Charsets;
import net.amygdalum.patternsearchalgorithms.pattern.PatternRule.Only;

@Only({ MATCH, SEARCH })
@Charsets({"UTF-8", "CHARS"})
public class PatternMatchTest {

	private static final char MAX_VALUE_DEC = before(MAX_VALUE);
	private static final char MIN_VALUE_INC = after(MIN_VALUE);

	@Rule
	public PatternRule patterns = new PatternRule();

	@Test
	public void testNonASCII2Bytes() throws Exception {
		Pattern pattern = patterns.compile("\u0085");
		assertTrue(pattern.matcher("\u0085").matches());
	}

	@Test
	public void testNonASCII3Bytes() throws Exception {
		Pattern pattern = patterns.compile("\u2028");
		assertTrue(pattern.matcher("\u2028").matches());
	}

	@Test
	public void testNonASCIIComp2Bytes() throws Exception {
		Pattern pattern = patterns.compile("[^\u0085]");
		assertFalse(pattern.matcher("\u0085").matches());
	}

	@Test
	public void testNonASCIIComp3Bytes() throws Exception {
		Pattern pattern = patterns.compile("[^\u2028]");
		assertFalse(pattern.matcher("\u2028").matches());
	}

	@Test
	public void testDotNotMatchesAll() throws Exception {
		Pattern pattern = patterns.compile(".+");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("b\n").matches());
		assertFalse(pattern.matcher("b\r").matches());
		assertFalse(pattern.matcher("b\u0085").matches());
		assertFalse(pattern.matcher("b\u2028").matches());
		assertFalse(pattern.matcher("b\u2029").matches());
	}

	@Test
	public void testDotMatchesAll() throws Exception {
		Pattern pattern = patterns.compile(".*", DOT_ALL);
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("aa").matches());
		assertTrue(pattern.matcher("b\n").matches());
		assertTrue(pattern.matcher("b\r").matches());
		assertTrue(pattern.matcher("b\u0085").matches());
		assertTrue(pattern.matcher("b\u2028").matches());
		assertTrue(pattern.matcher("b\u2029").matches());
	}

	@Test
	public void testAlternatives() throws Exception {
		Pattern pattern = patterns.compile("a|b");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("b").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("ab").matches());
	}

	@Test
	public void testConcat() throws Exception {
		Pattern pattern = patterns.compile("ab*");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("ab").matches());
		assertTrue(pattern.matcher("abb").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("b").matches());
	}

	@Test
	public void testLoop() throws Exception {
		Pattern pattern = patterns.compile("a{1,2}");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("aaa").matches());
	}

	@Test
	public void testMoreItemsLoop() throws Exception {
		Pattern pattern = patterns.compile("a{1,3}");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("aa").matches());
		assertTrue(pattern.matcher("aaa").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("aaaa").matches());
	}

	@Test
	public void testStarLoop() throws Exception {
		Pattern pattern = patterns.compile("a*");
		assertTrue(pattern.matcher("").matches());
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("b").matches());
	}

	@Test
	public void testPlusLoop() throws Exception {
		Pattern pattern = patterns.compile("a+");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("b").matches());
	}

	@Test
	public void testOptional() throws Exception {
		Pattern pattern = patterns.compile("a?");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("").matches());
		assertFalse(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("b").matches());
	}

	@Test
	public void testAnyChar() throws Exception {
		Pattern pattern = patterns.compile(".");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("c").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("ab").matches());
		assertFalse(pattern.matcher("bc").matches());
	}

	@Test
	public void testRangeChar() throws Exception {
		Pattern pattern = patterns.compile("[b-c]");
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("c").matches());
		assertFalse(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("d").matches());
		assertFalse(pattern.matcher("bc").matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testSingleChar() throws Exception {
		Pattern pattern = patterns.compile("a");
		assertTrue(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testString() throws Exception {
		Pattern pattern = patterns.compile("abc");
		assertTrue(pattern.matcher("abc").matches());
		assertFalse(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("b").matches());
		assertFalse(pattern.matcher("c").matches());
		assertFalse(pattern.matcher("abcd").matches());
	}

	@Test
	public void testEmpty() throws Exception {
		Pattern pattern = patterns.compile("");
		assertTrue(pattern.matcher("").matches());
		assertFalse(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("b").matches());
	}

	@Test
	public void testGroup() throws Exception {
		Pattern pattern = patterns.compile("(a)");
		assertTrue(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("aa").matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testMatchAllIdempotent() throws Exception {
		Pattern pattern = patterns.compile("ab*c", ALL);
		Matcher matcher = pattern.matcher("ac");
		assertTrue(matcher.matches());
		assertTrue(matcher.matches());
	}

	@Test
	public void testMatchLongestNonOverlappingIdempotent() throws Exception {
		Pattern pattern = patterns.compile("ab*c", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("ac");
		assertTrue(matcher.matches());
		assertTrue(matcher.matches());
	}

	@Test
	public void testMatchLongestOverlappingIdempotent() throws Exception {
		Pattern pattern = patterns.compile("ab*c", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("ac");
		assertTrue(matcher.matches());
		assertTrue(matcher.matches());
	}

	@Test
	public void testMatchFirstNonOverlappingIdempotent() throws Exception {
		Pattern pattern = patterns.compile("ab*c", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("ac");
		assertTrue(matcher.matches());
		assertTrue(matcher.matches());
	}

	@Test
	public void testMatchPattern1() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("ac");
		assertTrue(matcher.matches());
		assertThat(matcher.group(), equalTo("ac"));
		matcher = pattern.matcher("abc");
		assertTrue(matcher.matches());
		assertThat(matcher.group(), equalTo("abc"));
		matcher = pattern.matcher("abbc");
		assertTrue(matcher.matches());
		assertThat(matcher.group(), equalTo("abbc"));
		matcher = pattern.matcher("abbbc");
		assertTrue(matcher.matches());
		assertThat(matcher.group(), equalTo("abbbc"));
		matcher = pattern.matcher("c");
		assertFalse(matcher.matches());
		assertThat(matcher.group(), nullValue());
	}

	@Test
	public void testMatchPattern2() throws Exception {
		Pattern pattern = patterns.compile("(([^:]+)://)?([^:/]+)(:([0-9]+))?(/.*)");
		Matcher matcher = pattern.matcher("http://www.linux.com/");
		assertTrue(matcher.matches());
		assertThat(matcher.group(), equalTo("http://www.linux.com/"));
		matcher = pattern.matcher("http://www.thelinuxshow.com/main.php3");
		assertTrue(matcher.matches());
		assertThat(matcher.group(), equalTo("http://www.thelinuxshow.com/main.php3"));
	}

	@Test
	public void testMatchPattern2_1() throws Exception {
		Pattern pattern = patterns.compile("(([^:]+)://)?");
		assertTrue(pattern.matcher("http://").matches());
		assertTrue(pattern.matcher("ftp://").matches());
		assertTrue(pattern.matcher("").matches());
	}

	@Test
	public void testMatchPattern2_2() throws Exception {
		Pattern pattern = patterns.compile("([^:/]+)");
		assertTrue(pattern.matcher("www.linux.com").matches());
		assertTrue(pattern.matcher("www.thelinuxshow.com").matches());
	}

	@Test
	public void testMatchSubmatchPattern8() throws Exception {
		Pattern pattern = patterns.compile("([a-e]|(abc))+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("cabcdecabc");
		boolean success = matcher.matches();
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
	public void testCharClassRange() throws Exception {
		Pattern pattern = patterns.compile("[a-e]");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("c").matches());
		assertTrue(pattern.matcher("d").matches());
		assertTrue(pattern.matcher("e").matches());
		assertFalse(pattern.matcher("" + before('a')).matches());
		assertFalse(pattern.matcher("" + after('e')).matches());
		assertFalse(pattern.matcher(".").matches());
		assertFalse(pattern.matcher("\n").matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCharClassWithSingleDash() throws Exception {
		Pattern pattern = patterns.compile("[b-]");
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("-").matches());
		assertFalse(pattern.matcher("a").matches());
	}

	@Test
	public void testCharClassWithEscapedChar() throws Exception {
		Pattern pattern = patterns.compile("[\\n]");
		assertTrue(pattern.matcher("\n").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("a").matches());
	}

	@Test
	public void testCharClassWithComplement() throws Exception {
		Pattern pattern = patterns.compile("[^bd]");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("c").matches());
		assertTrue(pattern.matcher("e").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("b").matches());
		assertFalse(pattern.matcher("d").matches());
	}

	@Test
	public void testCharClassWithComplementDotAll() throws Exception {
		Pattern pattern = patterns.compile("[^bd]", DOT_ALL);
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher("c").matches());
		assertTrue(pattern.matcher("e").matches());
		assertFalse(pattern.matcher("").matches());
		assertFalse(pattern.matcher("b").matches());
		assertFalse(pattern.matcher("d").matches());
	}

	@Test
	public void testCharClassMatchingDot() throws Exception {
		Pattern pattern = patterns.compile("[a\\.]+");
		assertTrue(pattern.matcher(".").matches());
		assertTrue(pattern.matcher("a.").matches());
		assertTrue(pattern.matcher(".a").matches());
		assertFalse(pattern.matcher("\n").matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMinValue() throws Exception {
		Pattern pattern = patterns.compile("[^" + MIN_VALUE + "]", DOT_ALL);
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE_INC)).matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMinValueInc() throws Exception {
		Pattern pattern = patterns.compile("[^" + MIN_VALUE_INC + "]", DOT_ALL);
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MIN_VALUE_INC)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMaxValue() throws Exception {
		Pattern pattern = patterns.compile("[^" + MAX_VALUE + "]", DOT_ALL);
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE_DEC)).matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMaxValueDec() throws Exception {
		Pattern pattern = patterns.compile("[^" + MAX_VALUE_DEC + "]", DOT_ALL);
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MAX_VALUE_DEC)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassLooping() throws Exception {
		Pattern pattern = patterns.compile("[^ce]+");
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("d").matches());
		assertTrue(pattern.matcher("f").matches());
		assertTrue(pattern.matcher("ff").matches());
		assertTrue(pattern.matcher("dd").matches());
		assertTrue(pattern.matcher("bb").matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testOverlappingCharClassRanges() throws Exception {
		Pattern pattern = patterns.compile("[b-cc-d]");
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("c").matches());
		assertTrue(pattern.matcher("d").matches());
		assertFalse(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("e").matches());
	}

	@Test
	public void testOverlappingCharClassRangesSameStart() throws Exception {
		Pattern pattern = patterns.compile("[b-cb-d]");
		assertTrue(pattern.matcher("b").matches());
		assertTrue(pattern.matcher("c").matches());
		assertTrue(pattern.matcher("d").matches());
		assertFalse(pattern.matcher("a").matches());
		assertFalse(pattern.matcher("e").matches());
	}

	@Test
	public void testBoundedLoop() throws Exception {
		Pattern pattern = patterns.compile("a{4,6}");
		assertTrue(pattern.matcher("aaaa").matches());
		assertTrue(pattern.matcher("aaaaa").matches());
		assertTrue(pattern.matcher("aaaaaa").matches());
		assertFalse(pattern.matcher("aaa").matches());
		assertFalse(pattern.matcher("aaaaaaa").matches());
		assertFalse(pattern.matcher("aaaaaaaa").matches());
		assertFalse(pattern.matcher("aaaaaaaaaaaa").matches());
	}

	@Test(expected = RuntimeException.class)
	public void testUnclosedCharClass() throws Exception {
		patterns.compile("[a-b");
	}

	@Test(expected = RuntimeException.class)
	public void testUnclosedLoop() throws Exception {
		patterns.compile("a{1,2");
	}

	@Test(expected = RuntimeException.class)
	public void testOpenBracket() throws Exception {
		patterns.compile("[");
	}
	
}