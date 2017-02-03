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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.amygdalum.patternsearchalgorithms.pattern.OptimizationTargetRule.Only;

@Only({ MATCH, SEARCH })
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
	public void testDotNotFindsAll() throws Exception {
		Pattern pattern = patterns.compile(".+");
		Matcher bna = pattern.matcher("b\na");
		assertTrue(bna.find());
		assertThat(bna.group(), equalTo("b"));
		assertTrue(bna.find());
		assertThat(bna.group(), equalTo("a"));
		Matcher bn = pattern.matcher("b\n");
		assertTrue(bn.find());
		assertThat(bn.group(), equalTo("b"));
		assertFalse(bn.find());
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
	public void testFindPattern1midMatch() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("xxxabbbbcxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(9l));
		assertThat(matcher.group(), equalTo("abbbbc"));
	}

	@Test
	public void testFindPattern1deadEnd() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("abbxxxabbcxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(6l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("abbc"));
	}

	@Test
	public void testFindPattern4longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("aba", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern4longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("aba", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(5l));
		assertThat(matcher.end(), equalTo(8l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern4firstMatchNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("aba", SearchMode.FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern4all() throws Exception {
		Pattern pattern = patterns.compile("aba", ALL);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(5l));
		assertThat(matcher.end(), equalTo(8l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern5longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("abababa"));
		assertThat(matcher.groups(), contains("abababa"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern5longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("abababa"));
		assertThat(matcher.groups(), contains("abababa"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern5firstMatchNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", SearchMode.FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("a"));
		assertThat(matcher.groups(), contains("a"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("b"));
		assertThat(matcher.groups(), contains("b"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("a"));
		assertThat(matcher.groups(), contains("a"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("b"));
		assertThat(matcher.groups(), contains("b"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("a"));
		assertThat(matcher.groups(), contains("a"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("b"));
		assertThat(matcher.groups(), contains("b"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("a"));
		assertThat(matcher.groups(), contains("a"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testFindPattern5all() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", ALL);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("a"));
		assertThat(matcher.groups(), containsInAnyOrder("a"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("ab"));
		assertThat(matcher.groups(), containsInAnyOrder("ab","b"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("aba"));
		assertThat(matcher.groups(), containsInAnyOrder("aba","ba","a"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("abab"));
		assertThat(matcher.groups(), containsInAnyOrder("abab","bab","ab","b"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("ababa"));
		assertThat(matcher.groups(), containsInAnyOrder("ababa","baba","aba","ba","a"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("ababab"));
		assertThat(matcher.groups(), containsInAnyOrder("ababab","babab","abab","bab","ab","b"));
		success = matcher.find();
		assertThat(matcher.group(), equalTo("abababa"));
		assertThat(matcher.groups(), containsInAnyOrder("abababa","bababa","ababa","baba","aba","ba","a"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testPrefixPattern7longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ca|cabc|bcdec|ec", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xxxcabcdecabcxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(7l));
		assertThat(matcher.group(), equalTo("cabc"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.group(), equalTo("bcdec"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.group(), equalTo("cabc"));
		success = matcher.find();
		assertFalse(success);
	}

	@Test
	public void testPrefixPattern7longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ca|cabc|bcdec|ec", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxcabcdecabcxxxx");
		boolean success = matcher.find();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(7l));
		assertThat(matcher.group(), equalTo("cabc"));
		success = matcher.find();
		assertTrue(success);
		assertThat(matcher.group(), equalTo("ec"));
		success = matcher.find();
		assertFalse(success);
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
	public void testPrefixPattern5simple() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+");
		Matcher matcher = pattern.matcher("axxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testPrefixPattern5longest() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("abaxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(3l));
		assertThat(matcher.group(), equalTo("aba"));
	}

	@Test
	public void testPrefixPattern5first() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("abaxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testPrefixPattern6all() throws Exception {
		Pattern pattern = patterns.compile("cabc|ab");
		Matcher matcher = pattern.matcher("cabcxxxx");
		boolean success = matcher.prefixes();
		assertTrue(success);
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("cabc"));
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
	public void testCompClassWithMinValue() throws Exception {
		Pattern pattern = patterns.compile("[^" + MIN_VALUE + "]");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE_INC)).matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMinValueInc() throws Exception {
		Pattern pattern = patterns.compile("[^" + MIN_VALUE_INC + "]");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MIN_VALUE_INC)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMaxValue() throws Exception {
		Pattern pattern = patterns.compile("[^" + MAX_VALUE + "]");
		assertTrue(pattern.matcher("a").matches());
		assertTrue(pattern.matcher(String.valueOf(MAX_VALUE_DEC)).matches());
		assertTrue(pattern.matcher(String.valueOf(MIN_VALUE)).matches());
		assertFalse(pattern.matcher(String.valueOf(MAX_VALUE)).matches());
		assertFalse(pattern.matcher("").matches());
	}

	@Test
	public void testCompClassWithMaxValueDec() throws Exception {
		Pattern pattern = patterns.compile("[^" + MAX_VALUE_DEC + "]");
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
	public void testCharClassMatchingDot() throws Exception {
		Pattern pattern = patterns.compile("[a\\.]+");
		assertTrue(pattern.matcher(".").matches());
		assertTrue(pattern.matcher("a.").matches());
		assertTrue(pattern.matcher(".a").matches());
		assertFalse(pattern.matcher("\n").matches());
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