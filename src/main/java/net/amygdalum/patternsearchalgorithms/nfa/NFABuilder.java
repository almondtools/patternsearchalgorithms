package net.amygdalum.patternsearchalgorithms.nfa;

import static java.util.Arrays.asList;
import static net.amygdalum.util.text.ByteEncoding.encode;
import static net.amygdalum.util.text.ByteEncoding.intervals;
import static net.amygdalum.util.text.ByteUtils.after;
import static net.amygdalum.util.text.ByteUtils.before;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.amygdalum.patternsearchalgorithms.nfa.NFABuilder.PartialNFA;
import net.amygdalum.regexparser.AlternativesNode;
import net.amygdalum.regexparser.AnyCharNode;
import net.amygdalum.regexparser.BoundedLoopNode;
import net.amygdalum.regexparser.CharClassNode;
import net.amygdalum.regexparser.CompClassNode;
import net.amygdalum.regexparser.ConcatNode;
import net.amygdalum.regexparser.EmptyNode;
import net.amygdalum.regexparser.GroupNode;
import net.amygdalum.regexparser.OptionalNode;
import net.amygdalum.regexparser.RangeCharNode;
import net.amygdalum.regexparser.RegexNode;
import net.amygdalum.regexparser.RegexNodeVisitor;
import net.amygdalum.regexparser.SingleCharNode;
import net.amygdalum.regexparser.SpecialCharClassNode;
import net.amygdalum.regexparser.StringNode;
import net.amygdalum.regexparser.UnboundedLoopNode;
import net.amygdalum.util.text.ByteRange;

public class NFABuilder implements RegexNodeVisitor<PartialNFA> {

	private static final byte MAXBYTE = (byte) 255;
	private static final byte MINBYTE = (byte) 0;

	private RegexNode node;
	private Charset charset;

	public NFABuilder(RegexNode node, Charset charset) {
		this.node = node;
		this.charset = charset;
	}

	public PartialNFA match(char value) {
		State s = new State();
		State e = new State();
		connect(s, e, value);
		return new PartialNFA(s, e);
	}

	public PartialNFA match(String value) {
		State s = new State();
		State e = new State();
		connect(s, e, value);
		return new PartialNFA(s, e);
	}

	public PartialNFA match(char from, char to) {
		State s = new State();
		State e = new State();
		if (from > to) {
			char temp = from;
			from = to;
			to = temp;
		}
		connect(s, e, from, to);
		return new PartialNFA(s, e);
	}

	private void connect(State s, State e, String value) {
		connect(s, e, encode(value, charset));
	}

	private void connect(State s, State e, char value) {
		connect(s, e, encode(charset, value));
	}

	private void connect(State s, State e, byte[] bytes) {
		if (bytes.length == 0) {
			//do nothing
		} else if (bytes.length == 1) {
			s.addTransition(new ByteTransition(s, bytes[0], e));
		} else {
			int length = bytes.length;

			State[] chain = new State[length - 1];
			for (int i = 0; i < chain.length; i++) {
				chain[i] = new State();
			}
			int last = chain.length - 1;

			s.addTransition(new ByteTransition(s, bytes[0], chain[0]));

			for (int i = 1; i < length - 1; i++) {
				chain[i - 1].addTransition(new ByteTransition(chain[i - 1], bytes[i], chain[i]));
			}

			chain[last].addTransition(new ByteTransition(chain[last], bytes[bytes.length - 1], e));
		}
	}

	private void connect(State s, State e, char from, char to) {
		for (ByteRange bytes : intervals(charset, from, to)) {
			connect(s, e, bytes);
		}
	}

	private void connect(State s, State e, ByteRange bytes) {
		int length = bytes.from.length;

		if (length == 0) {
			// do nothing
		} else if (length == 1) {
			s.addTransition(new BytesTransition(s, bytes.from[0], bytes.to[0], e));
		} else {
			State[][] chain = new State[length - 1][3];
			for (int j = 0; j < 3; j++) {
				for (int i = 0; i < chain.length; i++) {
					chain[i][j] = new State();
				}
			}
			int last = chain.length - 1;

			s.addTransition(new ByteTransition(s, bytes.from[0], chain[0][0]));
			s.addTransition(new BytesTransition(s, after(bytes.from[0]), before(bytes.to[0]), chain[0][1]));
			s.addTransition(new ByteTransition(s, bytes.to[0], chain[0][2]));

			for (int i = 1; i < length - 1; i++) {
				chain[i - 1][0].addTransition(new ByteTransition(chain[i - 1][0], bytes.from[i], chain[i][0]));
				if (after(bytes.from[i]) != MAXBYTE) {
					chain[i - 1][0].addTransition(new BytesTransition(chain[i - 1][0], after(bytes.from[i]), MAXBYTE, chain[i][1]));
				}

				chain[i - 1][1].addTransition(new BytesTransition(chain[i - 1][1], MINBYTE, MAXBYTE, chain[i][1]));

				if (MINBYTE != before(bytes.from[i])) {
					chain[i - 1][2].addTransition(new BytesTransition(chain[i - 1][2], MINBYTE, before(bytes.to[i]), chain[i][1]));
				}
				chain[i - 1][2].addTransition(new ByteTransition(chain[i - 1][2], bytes.to[i], chain[i][2]));
			}

			chain[last][0].addTransition(new BytesTransition(chain[last][0], bytes.from[length - 1], MAXBYTE, e));
			chain[last][1].addTransition(new BytesTransition(chain[last][1], MINBYTE, MAXBYTE, e));
			chain[last][2].addTransition(new BytesTransition(chain[last][2], MINBYTE, bytes.to[length - 1], e));

		}
	}

	public PartialNFA matchGroup(PartialNFA a) {
		return a;
	}

	public PartialNFA matchAlternatives(List<PartialNFA> as) {
		if (as.size() == 1) {
			return as.get(0);
		}
		State s = new State();
		State e = new State();
		for (PartialNFA a : as) {
			State n = a.start;
			s.addTransition(new EpsilonTransition(s, n));
			a.end.addTransition(new EpsilonTransition(a.end, e));
		}
		return new PartialNFA(s, e);
	}

	public PartialNFA matchConcatenation(List<PartialNFA> as) {
		if (as.size() == 1) {
			return as.get(0);
		}

		State s = as.get(0).start;
		State e = as.get(as.size() - 1).end;

		State last = null;
		ListIterator<PartialNFA> aIterator = as.listIterator();
		while (aIterator.hasNext()) {
			PartialNFA a = aIterator.next();
			if (last != null) {
				last.addTransition(new EpsilonTransition(last, a.start));
			}
			last = a.end;
		}
		return new PartialNFA(s, e);
	}

	public PartialNFA matchEmpty() {
		State s = new State();
		return new PartialNFA(s, s);
	}

	public PartialNFA matchNothing() {
		State s = new State();
		return new PartialNFA(s, null);
	}

	public PartialNFA matchOptional(PartialNFA a) {
		State s = new State();
		State e = new State();
		s.addTransition(new EpsilonTransition(s, e));
		s.addTransition(new EpsilonTransition(s, a.start));
		a.end.addTransition(new EpsilonTransition(a.end, e));
		return new PartialNFA(s, e);
	}

	public PartialNFA matchUnlimitedLoop(PartialNFA a, int start) {
		if (start == 0) {
			return matchStarLoop(a);
		} else {
			List<PartialNFA> as = copyOf(a, start);
			as.add(matchStarLoop(a.clone()));
			return matchConcatenation(as);
		}
	}

	public PartialNFA matchStarLoop(PartialNFA a) {
		State s = new State();
		State e = new State();
		s.addTransition(new EpsilonTransition(s, a.start));
		s.addTransition(new EpsilonTransition(s, e));
		a.end.addTransition(new EpsilonTransition(a.end, a.start));
		a.end.addTransition(new EpsilonTransition(a.end, e));
		return new PartialNFA(s, e);
	}

	public PartialNFA matchRangeLoop(PartialNFA a, int start, int end) {
		if (start == end) {
			return matchFixedLoop(a, start);
		} else {
			PartialNFA aFixed = matchFixedLoop(a, start);
			PartialNFA aUpToN = matchUpToN(a.clone(), end - start);
			PartialNFA matchConcatenation = matchConcatenation(asList(aFixed, aUpToN));
			return matchConcatenation;
		}
	}

	public PartialNFA matchFixedLoop(PartialNFA a, int count) {
		List<PartialNFA> as = copyOf(a, count);
		return matchConcatenation(as);
	}

	public PartialNFA matchUpToN(PartialNFA a, int count) {
		State s = new State();
		State e = new State();
		s.addTransition(new EpsilonTransition(s, e));

		State current = s;
		for (int i = 0; i < count; i++) {
			PartialNFA ai = a.clone();
			current.addTransition(new EpsilonTransition(current, ai.start));
			ai.end.addTransition(new EpsilonTransition(ai.end, e));
			current = ai.end;
		}
		return new PartialNFA(s, e);
	}

	private static List<PartialNFA> copyOf(PartialNFA a, int count) {
		List<PartialNFA> copies = new ArrayList<>(count);
		copies.add(a);
		for (int i = 1; i < count; i++) {
			copies.add(a.clone());
		}
		return copies;
	}

	@Override
	public PartialNFA visitAlternatives(AlternativesNode node) {
		List<PartialNFA> as = accept(node.getSubNodes());
		return matchAlternatives(as);
	}

	@Override
	public PartialNFA visitAnyChar(AnyCharNode node) {
		List<PartialNFA> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public PartialNFA visitCharClass(CharClassNode node) {
		List<PartialNFA> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public PartialNFA visitCompClass(CompClassNode node) {
		List<PartialNFA> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public PartialNFA visitConcat(ConcatNode node) {
		List<PartialNFA> as = accept(node.getSubNodes());
		return matchConcatenation(as);
	}

	@Override
	public PartialNFA visitEmpty(EmptyNode node) {
		return matchEmpty();
	}

	@Override
	public PartialNFA visitGroup(GroupNode node) {
		return matchGroup(node.getSubNode().accept(this));
	}

	@Override
	public PartialNFA visitBoundedLoop(BoundedLoopNode node) {
		PartialNFA a = node.getSubNode().accept(this);
		int from = node.getFrom();
		int to = node.getTo();
		return matchRangeLoop(a, from, to);
	}

	@Override
	public PartialNFA visitUnboundedLoop(UnboundedLoopNode node) {
		PartialNFA a = node.getSubNode().accept(this);
		int from = node.getFrom();
		return matchUnlimitedLoop(a, from);
	}

	@Override
	public PartialNFA visitOptional(OptionalNode node) {
		PartialNFA a = node.getSubNode().accept(this);
		return matchOptional(a);
	}

	@Override
	public PartialNFA visitRangeChar(RangeCharNode node) {
		return match(node.getFrom(), node.getTo());
	}

	@Override
	public PartialNFA visitSingleChar(SingleCharNode node) {
		return match(node.getValue());
	}

	@Override
	public PartialNFA visitSpecialCharClass(SpecialCharClassNode node) {
		List<PartialNFA> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public PartialNFA visitString(StringNode node) {
		return match(node.getValue());
	}

	private List<PartialNFA> accept(List<? extends RegexNode> nodes) {
		List<PartialNFA> as = new ArrayList<PartialNFA>(nodes.size());
		for (RegexNode node : nodes) {
			as.add(node.accept(this));
		}
		return as;
	}

	public NFA build() {
		PartialNFA nfa = node.accept(this);
		return nfa.toFullNFA();
	}

	public class PartialNFA implements Cloneable {

		public State start;
		public State end;

		public PartialNFA(State start, State end) {
			this.start = start;
			this.end = end;
		}

		public NFA toFullNFA() {
			if (end != null) {
				end.accept();
			}
			return new NFA(start, charset);
		}

		@Override
		protected PartialNFA clone() {
			try {
				PartialNFA clone = (PartialNFA) super.clone();
				Map<State, State> clonedTree = start.cloneTree();
				clone.start = clonedTree.get(start);
				clone.end = clonedTree.get(end);
				return clone;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}
}
