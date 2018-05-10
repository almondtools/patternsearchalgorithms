package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import net.amygdalum.util.io.StringByteProvider;

public class NFADeterminizedTest {

	private NFABuilder nfaBuilder;

	@Before
	public void before() throws Exception {
		this.nfaBuilder = new NFABuilder(StandardCharsets.UTF_8);
	}

	@Test
	public void testMatchChar() throws Exception {
		NFA a = automatonOf(nfaBuilder.match('a'));
		assertThat(matchSamples(a, "a"), contains("a"));
		assertThat(matchSamples(a, "aa"), empty());
		assertThat(matchSamples(a, "b"), empty());
		assertThat(matchSamples(a, ""), empty());
	}

	@Test
	public void testMatchString() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match("abc"));
		assertThat(matchSamples(abc, "abc"), contains("abc"));
		assertThat(matchSamples(abc, "ab"), empty());
		assertThat(matchSamples(abc, "bc"), empty());
		assertThat(matchSamples(abc, "ac"), empty());
		assertThat(matchSamples(abc, ""), empty());
		assertThat(matchSamples(abc, "abcd"), empty());
	}

	@Test
	public void testMatchCharRange() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match('a', 'c'));
		assertThat(matchSamples(abc, "a"), contains("a"));
		assertThat(matchSamples(abc, "b"), contains("b"));
		assertThat(matchSamples(abc, "c"), contains("c"));
		assertThat(matchSamples(abc, "ab"), empty());
		assertThat(matchSamples(abc, "bc"), empty());
		assertThat(matchSamples(abc, "ac"), empty());
		assertThat(matchSamples(abc, ""), empty());
		assertThat(matchSamples(abc, "abcd"), empty());
	}

	@Test
	public void testMatchReverseCharRange() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match('c', 'a'));
		assertThat(matchSamples(abc, "a"), contains("a"));
		assertThat(matchSamples(abc, "b"), contains("b"));
		assertThat(matchSamples(abc, "c"), contains("c"));
		assertThat(matchSamples(abc, "ab"), empty());
		assertThat(matchSamples(abc, "bc"), empty());
		assertThat(matchSamples(abc, "ac"), empty());
		assertThat(matchSamples(abc, ""), empty());
		assertThat(matchSamples(abc, "abcd"), empty());
	}

	@Test
	public void testMatchAnyChar() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match(Character.MIN_VALUE, Character.MAX_VALUE));
		assertThat(matchSamples(abc, "a"), contains("a"));
		assertThat(matchSamples(abc, "z"), contains("z"));
		assertThat(matchSamples(abc, "1"), contains("1"));
		assertThat(matchSamples(abc, "&"), contains("&"));
		assertThat(matchSamples(abc, "\n"), contains("\n"));
		assertThat(matchSamples(abc, "ab"), empty());
		assertThat(matchSamples(abc, "aa"), empty());
		assertThat(matchSamples(abc, "a\n"), empty());
		assertThat(matchSamples(abc, "a&b"), empty());
		assertThat(matchSamples(abc, ""), empty());
	}

	@Test
	public void testMatchAnyCharUnicodeReturns() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match(Character.MIN_VALUE, Character.MAX_VALUE));
		assertThat(matchSamples(abc, "\u0085"), contains("\u0085"));
		assertThat(matchSamples(abc, "\u2028"), contains("\u2028"));
		assertThat(matchSamples(abc, "\u2029"), contains("\u2029"));
	}

	@Test
	public void testMatchCharRangeDifferentByteSize() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match('\u0086', '\u2027'));
		assertThat(matchSamples(abc, "\u0086"), contains("\u0086"));
		assertThat(matchSamples(abc, "\u2027"), contains("\u2027"));
		assertThat(matchSamples(abc, "\u0085"), empty());
		assertThat(matchSamples(abc, "\u2028"), empty());
	}

	@Test
	public void testMatchAnyCharOf() throws Exception {
		NFA abc = automatonOf(nfaBuilder.match('a', 'b'));
		assertThat(matchSamples(abc, "a"), contains("a"));
		assertThat(matchSamples(abc, "b"), contains("b"));
		assertThat(matchSamples(abc, "c"), empty());
		assertThat(matchSamples(abc, "cc"), empty());
		assertThat(matchSamples(abc, ""), empty());
	}

	@Test
	public void testMatchNothing() throws Exception {
		NFA abc = automatonOf(nfaBuilder.matchNothing());
		assertThat(matchSamples(abc, ""), empty());
		assertThat(matchSamples(abc, "a"), empty());
		assertThat(matchSamples(abc, "ab"), empty());
	}

	@Test
	public void testMatchEmpty() throws Exception {
		NFA abc = automatonOf(nfaBuilder.matchEmpty());
		assertThat(matchSamples(abc, ""), contains(""));
		assertThat(matchSamples(abc, "a"), empty());
		assertThat(matchSamples(abc, "ab"), empty());
	}

	@Test
	public void testOptional() throws Exception {
		NFA ab_question = automatonOf(nfaBuilder.matchOptional(nfaBuilder.match("ab")));
		assertThat(matchSamples(ab_question, ""), contains(""));
		assertThat(matchSamples(ab_question, "ab"), contains("ab"));
		assertThat(matchSamples(ab_question, "a"), empty());
		assertThat(matchSamples(ab_question, "b"), empty());
		assertThat(matchSamples(ab_question, "abc"), empty());
	}

	@Test
	public void testUnlimitedLoop0() throws Exception {
		NFA a_star = automatonOf(nfaBuilder.matchUnlimitedLoop(nfaBuilder.match('a'), 0));
		assertThat(matchSamples(a_star, ""), contains(""));
		assertThat(matchSamples(a_star, "a"), contains("a"));
		assertThat(matchSamples(a_star, "aa"), contains("aa"));
		assertThat(matchSamples(a_star, "aaaaaaaaaaaaaaaaaaaaa"), contains("aaaaaaaaaaaaaaaaaaaaa"));
		assertThat(matchSamples(a_star, "ab"), empty());
	}

	@Test
	public void testUnlimitedLoop1() throws Exception {
		NFA a_plus = automatonOf(nfaBuilder.matchUnlimitedLoop(nfaBuilder.match('a'), 1));
		assertThat(matchSamples(a_plus, "a"), contains("a"));
		assertThat(matchSamples(a_plus, "aa"), contains("aa"));
		assertThat(matchSamples(a_plus, "aaaaaaaaaaaaaaaaaaaaa"), contains("aaaaaaaaaaaaaaaaaaaaa"));
		assertThat(matchSamples(a_plus, ""), empty());
		assertThat(matchSamples(a_plus, "ab"), empty());
	}

	@Test
	public void testUnlimitedLoopN() throws Exception {
		NFA a_minN = automatonOf(nfaBuilder.matchUnlimitedLoop(nfaBuilder.match('a'), 4));
		assertThat(matchSamples(a_minN, "aaaa"), contains("aaaa"));
		assertThat(matchSamples(a_minN, "aaaaaaaaaaaaaaaaaaaaa"), contains("aaaaaaaaaaaaaaaaaaaaa"));
		assertThat(matchSamples(a_minN, ""), empty());
		assertThat(matchSamples(a_minN, "aaa"), empty());
		assertThat(matchSamples(a_minN, "ab"), empty());
	}

	@Test
	public void testRangeLoop() throws Exception {
		NFA a_1_2 = automatonOf(nfaBuilder.matchRangeLoop(nfaBuilder.match('a'), 1, 2));
		assertThat(matchSamples(a_1_2, "a"), contains("a"));
		assertThat(matchSamples(a_1_2, "aa"), contains("aa"));
		assertThat(matchSamples(a_1_2, ""), empty());
		assertThat(matchSamples(a_1_2, "aaaaaaaaaaaaaaaaaaaaa"), empty());
		assertThat(matchSamples(a_1_2, "ab"), empty());
	}

	@Test
	public void testBroadRangeLoop() throws Exception {
		NFA a_2_4 = automatonOf(nfaBuilder.matchRangeLoop(nfaBuilder.match('a'), 2, 4));
		assertThat(matchSamples(a_2_4, "aa"), contains("aa"));
		assertThat(matchSamples(a_2_4, "aaa"), contains("aaa"));
		assertThat(matchSamples(a_2_4, "aaaa"), contains("aaaa"));
		assertThat(matchSamples(a_2_4, ""), empty());
		assertThat(matchSamples(a_2_4, "aaaaaaaaaaaaaaaaaaaaa"), empty());
		assertThat(matchSamples(a_2_4, "ab"), empty());
	}

	@Test
	public void testFixedRangeLoop() throws Exception {
		NFA a_1_1 = automatonOf(nfaBuilder.matchRangeLoop(nfaBuilder.match('a'), 1, 1));
		assertThat(matchSamples(a_1_1, "a"), contains("a"));
		assertThat(matchSamples(a_1_1, "aa"), empty());
		assertThat(matchSamples(a_1_1, ""), empty());
		assertThat(matchSamples(a_1_1, "aaaaaaaaaaaaaaaaaaaaa"), empty());
	}

	@Test
	public void testMatchFixedLoop() throws Exception {
		NFA a_2 = automatonOf(nfaBuilder.matchFixedLoop(nfaBuilder.match('a'), 2));
		assertThat(matchSamples(a_2, "aa"), contains("aa"));
		assertThat(matchSamples(a_2, ""), empty());
		assertThat(matchSamples(a_2, "a"), empty());
		assertThat(matchSamples(a_2, "aaaaaaaaaaaaaaaaaaaaa"), empty());
		assertThat(matchSamples(a_2, "ab"), empty());
	}

	@Test
	public void testMatchConcatenation() throws Exception {
		NFA aAndb = automatonOf(nfaBuilder.matchConcatenation(asList(nfaBuilder.match('a'), nfaBuilder.match('b'))));
		assertThat(matchSamples(aAndb, "ab"), contains("ab"));
		assertThat(matchSamples(aAndb, ""), empty());
		assertThat(matchSamples(aAndb, "a"), empty());
		assertThat(matchSamples(aAndb, "b"), empty());
		assertThat(matchSamples(aAndb, "abab"), empty());
	}

	@Test
	public void testMatchAlternatives() throws Exception {
		NFA aOrb = automatonOf(nfaBuilder.matchAlternatives(asList(nfaBuilder.match('a'), nfaBuilder.match('b'))));
		assertThat(matchSamples(aOrb, "a"), contains("a"));
		assertThat(matchSamples(aOrb, "b"), contains("b"));
		assertThat(matchSamples(aOrb, ""), empty());
		assertThat(matchSamples(aOrb, "ab"), empty());
		assertThat(matchSamples(aOrb, "abab"), empty());
	}

	@Test
	public void testMatchAlternativesOne() throws Exception {
		NFA aOrb = automatonOf(nfaBuilder.matchAlternatives(asList(nfaBuilder.match('a'))));
		assertThat(matchSamples(aOrb, "a"), contains("a"));
		assertThat(matchSamples(aOrb, ""), empty());
		assertThat(matchSamples(aOrb, "b"), empty());
		assertThat(matchSamples(aOrb, "ab"), empty());
	}

	@Test
	public void testMatchNonASCIIExpression() throws Exception {
		NFA aOrb = automatonOf(nfaBuilder.matchAlternatives(asList(nfaBuilder.match('ä'), nfaBuilder.match('ö'), nfaBuilder.matchUnlimitedLoop(nfaBuilder.match('ü'), 1))));
		assertThat(matchSamples(aOrb, "ä"), contains("ä"));
		assertThat(matchSamples(aOrb, "ö"), contains("ö"));
		assertThat(matchSamples(aOrb, "ü"), contains("ü"));
		assertThat(matchSamples(aOrb, "üü"), contains("üü"));
		assertThat(matchSamples(aOrb, "üüü"), contains("üüü"));
		assertThat(matchSamples(aOrb, ""), empty());
		assertThat(matchSamples(aOrb, "a"), empty());
		assertThat(matchSamples(aOrb, "o"), empty());
		assertThat(matchSamples(aOrb, "u"), empty());
		assertThat(matchSamples(aOrb, "üaö"), empty());
	}

	private static NFA automatonOf(NFAComponent automaton) {
		NFA nfa = new NFABuilder(UTF_8).build(automaton);
		nfa.determinize();
		return nfa;
	}

	public static Set<String> matchSamples(NFA a, String... samples) {
		Set<String> matched = new HashSet<>();
		for (String sample : samples) {
			NFAMatcher m = new NFAMatcher(a, new StringByteProvider(sample, 0, a.getCharset()));
			if (m.matches()) {
				matched.add(sample);
			}
		}
		return matched;
	}

}
