package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.MIN_VALUE;
import static java.util.Arrays.asList;
import static net.amygdalum.util.text.CharUtils.after;
import static net.amygdalum.util.text.CharUtils.before;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Test;

import net.amygdalum.util.text.CharRange;

public class SmallRangeCharClassMapperTest {

	@Test
	public void testFullRangeCharClass() throws Exception {
		SmallRangeCharClassMapper mapper = new SmallRangeCharClassMapper(asList(new CharRange(MIN_VALUE, MAX_VALUE)));
		assertThat(mapper.getIndex(MIN_VALUE), equalTo(0));
		assertThat(mapper.getIndex('5'), equalTo(0));
		assertThat(mapper.getIndex('a'), equalTo(0));
		assertThat(mapper.getIndex('ä'), equalTo(0));
		assertThat(mapper.getIndex(MAX_VALUE), equalTo(0));
		assertThat(mapper.indexCount(), equalTo(1));
		assertThat(mapper.representative(0), equalTo(MIN_VALUE));
	}

	@Test
	public void testEmptyRangeCharClass() throws Exception {
		SmallRangeCharClassMapper mapper = new SmallRangeCharClassMapper(Collections.<CharRange>emptyList());
		assertThat(mapper.getIndex(MIN_VALUE), equalTo(0));
		assertThat(mapper.getIndex('5'), equalTo(0));
		assertThat(mapper.getIndex('a'), equalTo(0));
		assertThat(mapper.getIndex('ä'), equalTo(0));
		assertThat(mapper.getIndex(MAX_VALUE), equalTo(0));
		assertThat(mapper.indexCount(), equalTo(1));
		assertThat(mapper.representative(0), equalTo(MIN_VALUE));
	}

	@Test
	public void testPrefixRangeCharClass() throws Exception {
		SmallRangeCharClassMapper mapper = new SmallRangeCharClassMapper(asList(new CharRange(MIN_VALUE, 'a')));
		assertThat(mapper.getIndex(MIN_VALUE), equalTo(1));
		assertThat(mapper.getIndex('5'), equalTo(1));
		assertThat(mapper.getIndex('a'), equalTo(1));
		assertThat(mapper.getIndex('ä'), equalTo(0));
		assertThat(mapper.getIndex(MAX_VALUE), equalTo(0));
		assertThat(mapper.indexCount(), equalTo(2));
		assertThat(mapper.representative(0), equalTo(after('a')));
		assertThat(mapper.representative(1), equalTo(MIN_VALUE));
	}

	@Test
	public void testSuffixRangeCharClass() throws Exception {
		SmallRangeCharClassMapper mapper = new SmallRangeCharClassMapper(asList(new CharRange('a', MAX_VALUE)));
		assertThat(mapper.getIndex(MIN_VALUE), equalTo(0));
		assertThat(mapper.getIndex('5'), equalTo(0));
		assertThat(mapper.getIndex('a'), equalTo(1));
		assertThat(mapper.getIndex('ä'), equalTo(1));
		assertThat(mapper.getIndex(MAX_VALUE), equalTo(1));
		assertThat(mapper.indexCount(), equalTo(2));
		assertThat(mapper.representative(0), equalTo(MIN_VALUE));
		assertThat(mapper.representative(1), equalTo('a'));
	}

	@Test
	public void testInfixRangeCharClass() throws Exception {
		SmallRangeCharClassMapper mapper = new SmallRangeCharClassMapper(asList(new CharRange('5', 'ä')));
		assertThat(mapper.getIndex(MIN_VALUE), equalTo(0));
		assertThat(mapper.getIndex('5'), equalTo(1));
		assertThat(mapper.getIndex('a'), equalTo(1));
		assertThat(mapper.getIndex('ä'), equalTo(1));
		assertThat(mapper.getIndex(MAX_VALUE), equalTo(0));
		assertThat(mapper.indexCount(), equalTo(2));
		assertThat(mapper.representative(0), equalTo(MIN_VALUE));
		assertThat(mapper.representative(1), equalTo('5'));
	}

	@Test
	public void testInterruptedRangeCharClass() throws Exception {
		SmallRangeCharClassMapper mapper = new SmallRangeCharClassMapper(asList(new CharRange('5', before('a')), new CharRange(after('a'), 'ä')));
		assertThat(mapper.getIndex(MIN_VALUE), equalTo(0));
		assertThat(mapper.getIndex('5'), equalTo(1));
		assertThat(mapper.getIndex('a'), equalTo(0));
		assertThat(mapper.getIndex('ä'), equalTo(2));
		assertThat(mapper.getIndex(MAX_VALUE), equalTo(0));
		assertThat(mapper.indexCount(), equalTo(3));
		assertThat(mapper.representative(0), equalTo(MIN_VALUE));
		assertThat(mapper.representative(1), equalTo('5'));
		assertThat(mapper.representative(2), equalTo(after('a')));
	}

}
