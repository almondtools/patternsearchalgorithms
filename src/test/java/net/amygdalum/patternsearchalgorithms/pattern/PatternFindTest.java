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
@Charsets({ "UTF-8", "CHARS" })
public class PatternFindTest {

	@Rule
	public PatternRule patterns = new PatternRule();

	@Test
	public void testDotNotMatchesLineBreaks() throws Exception {
		Pattern pattern = patterns.compile(".+");
		Matcher bna = pattern.matcher("b\na");
		assertTrue(bna.find());
		assertThat(bna.group(), equalTo("b"));
		assertTrue(bna.find());
		assertThat(bna.group(), equalTo("a"));
	}

	@Test
	public void testDotAcceptsLinebreaksAtEnd() throws Exception {
		Pattern pattern = patterns.compile(".+");
		Matcher bn = pattern.matcher("b\n");
		assertTrue(bn.find());
		assertThat(bn.group(), equalTo("b"));
		assertFalse(bn.find());
	}

	@Test
	public void testFindPattern1midMatch() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("xxxabbbbcxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(9l));
		assertThat(matcher.group(), equalTo("abbbbc"));
	}

	@Test
	public void testFindPattern1deadEnd() throws Exception {
		Pattern pattern = patterns.compile("ab*c");
		Matcher matcher = pattern.matcher("abbxxxabbcxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(6l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("abbc"));
	}

	@Test
	public void testFindPattern2() throws Exception {
		Pattern pattern = patterns.compile("(([^:]+)://)?([^:/]+)(:([0-9]+))?(/.*)");
		Matcher matcher = pattern.matcher(""
			+ "http://www.linux.com/\n"
			+ "http://www.thelinuxshow.com/main.php3\n"
			+ "http");
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("http://www.linux.com/"));
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(21l));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("http://www.thelinuxshow.com/main.php3"));
		assertThat(matcher.start(), equalTo(22l));
		assertThat(matcher.end(), equalTo(59l));
	}

	@Test
	public void testFindPattern3all() throws Exception {
		Pattern pattern = patterns.compile("a*", ALL);
		Matcher matcher = pattern.matcher("xabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(0l));
		assertThat(matcher.group(), equalTo(""));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(1l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo(""));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(1l));
		assertThat(matcher.end(), equalTo(2l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testFindPattern3firstMatchNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("a*", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(0l));
		assertThat(matcher.group(), equalTo(""));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(1l));
		assertThat(matcher.end(), equalTo(1l));
		assertThat(matcher.group(), equalTo(""));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(1l));
		assertThat(matcher.end(), equalTo(2l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testFindPattern3longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("a*", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(0l));
		assertThat(matcher.group(), equalTo(""));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(1l));
		assertThat(matcher.end(), equalTo(2l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testFindPattern3longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("a*", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(0l));
		assertThat(matcher.end(), equalTo(0l));
		assertThat(matcher.group(), equalTo(""));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(1l));
		assertThat(matcher.end(), equalTo(2l));
		assertThat(matcher.group(), equalTo("a"));
	}

	@Test
	public void testFindPattern4longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("aba", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern4longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("aba", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(5l));
		assertThat(matcher.end(), equalTo(8l));
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern4firstMatchNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("aba", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern4all() throws Exception {
		Pattern pattern = patterns.compile("aba", ALL);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(6l));
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(5l));
		assertThat(matcher.end(), equalTo(8l));
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(7l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("aba"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern5longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("abababa"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern5longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(10l));
		assertThat(matcher.group(), equalTo("abababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("bababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("baba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern5firstMatchNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", FIRSTMATCH_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("a"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("b"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("b"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("b"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern5all() throws Exception {
		Pattern pattern = patterns.compile("(a|b)+", ALL);
		Matcher matcher = pattern.matcher("xxxabababacxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(4l));
		assertThat(matcher.group(), equalTo("a"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("abab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ababab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("abababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("b"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("bab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("baba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("babab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("bababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("abab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ababa"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("b"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("bab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("baba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ab"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("aba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("b"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ba"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("a"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern7longestOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ca|cabc|bcdec|ec", LONGEST_WITH_OVERLAP);
		Matcher matcher = pattern.matcher("xxxcabcdecabcxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(7l));
		assertThat(matcher.group(), equalTo("cabc"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("bcdec"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ec"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("cabc"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindPattern7longestNonOverlapping() throws Exception {
		Pattern pattern = patterns.compile("ca|cabc|bcdec|ec", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxcabcdecabcxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(7l));
		assertThat(matcher.group(), equalTo("cabc"));
		assertTrue(matcher.find());
		assertThat(matcher.group(), equalTo("ec"));
		assertFalse(matcher.find());
	}

	@Test
	public void testFindSubmatchPattern8() throws Exception {
		Pattern pattern = patterns.compile("([a-e]|(abc))+", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("xxxcabcdecabcxxxx");
		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(3l));
		assertThat(matcher.end(), equalTo(13l));
		assertThat(matcher.group(), equalTo("cabcdecabc"));
		assertThat(matcher.start(1), equalTo(10l));
		assertThat(matcher.end(1), equalTo(13l));
		assertThat(matcher.group(1), equalTo("abc"));
		assertThat(matcher.start(2), equalTo(10l));
		assertThat(matcher.end(2), equalTo(13l));
		assertThat(matcher.group(2), equalTo("abc"));
	}

	@Test
	public void testBug1() throws Exception {
		Pattern pattern = patterns.compile("ga{1,3}c(gc)?tc(gc)*", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("taatgaaacgactcatcagaccgcgtgctttcttagcgtagaagctgatgatcttaaatttgccgttcttctcatcgaggaacaccggcttgataatct");

		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(9l));
		assertThat(matcher.end(), equalTo(14l));
		assertThat(matcher.group(), equalTo("gactc"));
	}

	@Test
	public void testBug2() throws Exception {
		Pattern pattern = patterns.compile("ga{1,3}c(gc)?tc(gc)*", LONGEST_NON_OVERLAPPING);
		Matcher matcher = pattern.matcher("caaaagaaaaactcagggcgcgggcaacggcgttcgcttgaactccgctgaaaattatgccataggcgatgagcaaaaagacggcgaacagaacgccca");

		assertTrue(matcher.find());
		assertThat(matcher.start(), equalTo(39l));
		assertThat(matcher.end(), equalTo(45l));
		assertThat(matcher.group(), equalTo("gaactc"));
	}

}