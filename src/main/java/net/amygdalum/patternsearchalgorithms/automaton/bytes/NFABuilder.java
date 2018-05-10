package net.amygdalum.patternsearchalgorithms.automaton.bytes;

import static java.util.Arrays.asList;
import static net.amygdalum.util.text.ByteEncoding.encode;
import static net.amygdalum.util.text.ByteEncoding.intervals;
import static net.amygdalum.util.text.ByteUtils.after;
import static net.amygdalum.util.text.ByteUtils.before;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

public class NFABuilder implements RegexNodeVisitor<NFAComponent> {

	private static final byte MAXBYTE = (byte) 255;
	private static final byte MINBYTE = (byte) 0;

	private NFAComponentFactory factory;
	private Charset charset;
	private int groupIndex;

	public NFABuilder(Charset charset) {
		this(charset, new SimpleNFAComponentFactory());
	}

	public NFABuilder(Charset charset, NFAComponentFactory factory) {
		this.factory = factory;
		this.charset = charset;
		this.groupIndex = 0;
	}

	private int nextGroupIndex() {
		groupIndex++;
		return groupIndex;
	}

	public NFAComponent match(char value) {
		State s = new State();
		State e = new State();
		connect(s, e, value);
		return factory.create(s, e);
	}

	public NFAComponent match(String value) {
		State s = new State();
		State e = new State();
		connect(s, e, value);
		return factory.create(s, e);
	}

	public NFAComponent match(char from, char to) {
		State s = new State();
		State e = new State();
		if (from > to) {
			char temp = from;
			from = to;
			to = temp;
		}
		connect(s, e, from, to);
		return factory.create(s, e);
	}

	private void connect(State s, State e, byte[] bytes) {
		if (bytes.length == 0) {
			//do nothing
		} else if (bytes.length == 1) {
			new ByteTransition(s, bytes[0], e).connect();
		} else {
			int length = bytes.length;

			State[] chain = new State[length - 1];
			for (int i = 0; i < chain.length; i++) {
				chain[i] = new State();
			}
			int last = chain.length - 1;

			new ByteTransition(s, bytes[0], chain[0]).connect();

			for (int i = 1; i < length - 1; i++) {
				new ByteTransition(chain[i - 1], bytes[i], chain[i]).connect();
			}

			new ByteTransition(chain[last], bytes[bytes.length - 1], e).connect();
		}
	}

	private void connect(State s, State e, ByteRange bytes) {
		int length = bytes.from.length;

		if (length == 0) {
			// do nothing
		} else if (length == 1) {
			new BytesTransition(s, bytes.from[0], bytes.to[0], e).connect();
		} else {
			State[] states = new State[] { s };
			for (int i = 0; i < length - 1; i++) {
				states = connectState(states, bytes.from[i], bytes.to[i]);
			}
			connectState(states, bytes.from[length - 1], bytes.to[length - 1], e);
		}
	}

	private State[] connectState(State[] states, byte from, byte to) {
		if (states.length == 1) {
			if (from == to) {
				State[] next = new State[] { new State() };
				new ByteTransition(states[0], from, next[0]).connect();
				return next;
			} else if (to - from == 1) {
				State[] next = new State[] { new State(), new State() };
				new ByteTransition(states[0], from, next[0]).connect();
				new ByteTransition(states[0], to, next[1]).connect();
				return next;
			} else {
				State[] next = new State[] { new State(), new State(), new State() };
				new ByteTransition(states[0], from, next[0]).connect();
				new BytesTransition(states[0], after(from), before(to), next[1]).connect();
				new ByteTransition(states[0], to, next[2]).connect();
				return next;
			}
		} else if (states.length == 2) {
			if (from == MAXBYTE && to == MINBYTE) {
				State[] next = new State[] { new State(), new State() };
				new ByteTransition(states[0], from, next[0]).connect();
				new ByteTransition(states[1], to, next[1]).connect();
				return next;
			} else {
				State[] next = new State[] { new State(), new State(), new State() };
				new ByteTransition(states[0], from, next[0]).connect();
				if (from != MAXBYTE) {
					new BytesTransition(states[0], after(from), MAXBYTE, next[1]).connect();
				}
				if (to != MINBYTE) {
					new BytesTransition(states[1], MINBYTE, before(to), next[1]).connect();
				}
				new ByteTransition(states[1], to, next[2]).connect();
				return next;
			}
		} else if (states.length == 3) {
			State[] next = new State[] { new State(), new State(), new State() };
			new ByteTransition(states[0], from, next[0]).connect();
			if (from != MAXBYTE) {
				new BytesTransition(states[0], after(from), MAXBYTE, next[1]).connect();
			}
			new BytesTransition(states[1], MINBYTE, MAXBYTE, next[1]).connect();
			if (to != MINBYTE) {
				new BytesTransition(states[2], MINBYTE, before(to), next[1]).connect();
			}
			new ByteTransition(states[2], to, next[2]).connect();
			return next;
		} else {
			return new State[0];
		}
	}

	private void connectState(State[] states, byte from, byte to, State terminator) {
		if (states.length == 1 && from == to) {
			new ByteTransition(states[0], from, terminator).connect();
		} else if (states.length == 2) {
			if (from == MAXBYTE && to == MINBYTE) {
				new ByteTransition(states[0], from, terminator).connect();
				new ByteTransition(states[1], to, terminator).connect();
			} else {
				new ByteTransition(states[0], from, terminator).connect();
				if (from != MAXBYTE) {
					new BytesTransition(states[0], after(from), MAXBYTE, terminator).connect();
				}
				if (to != MINBYTE) {
					new BytesTransition(states[1], MINBYTE, before(to), terminator).connect();
				}
				new ByteTransition(states[1], to, terminator).connect();
			}
		} else if (states.length == 3) {
			new ByteTransition(states[0], from, terminator).connect();
			if (from != MAXBYTE) {
				new BytesTransition(states[0], after(from), MAXBYTE, terminator).connect();
			}
			new BytesTransition(states[1], MINBYTE, MAXBYTE, terminator).connect();
			if (to != MINBYTE) {
				new BytesTransition(states[2], MINBYTE, before(to), terminator).connect();
			}
			new ByteTransition(states[2], to, terminator).connect();
		}
	}

	private void connect(State s, State e, String value) {
		connect(s, e, encode(value, charset));
	}

	private void connect(State s, State e, char value) {
		connect(s, e, encode(charset, value));
	}

	private void connect(State s, State e, char from, char to) {
		for (ByteRange bytes : intervals(charset, from, to)) {
			connect(s, e, bytes);
		}
	}

	public NFAComponent matchGroup(NFAComponent a, int no) {
		State s = new State();
		State e = new State();
		new EpsilonTransition(s, a.start).withAction(new StartGroup(no)).connect();
		new EpsilonTransition(a.end, e).withAction(new EndGroup(no)).connect();
		return factory.create(s, e);
	}

	public NFAComponent matchAlternatives(List<NFAComponent> as) {
		if (as.size() == 1) {
			return as.get(0);
		}
		State s = new State();
		State e = new State();
		for (NFAComponent a : as) {
			State n = a.start;
			new EpsilonTransition(s, n).connect();
			new EpsilonTransition(a.end, e).connect();
		}
		return factory.create(s, e);
	}

	public NFAComponent matchConcatenation(List<NFAComponent> as) {
		if (as.size() == 1) {
			return as.get(0);
		}

		State s = as.get(0).start;
		State e = as.get(as.size() - 1).end;

		State last = null;
		ListIterator<NFAComponent> aIterator = as.listIterator();
		while (aIterator.hasNext()) {
			NFAComponent a = aIterator.next();
			if (last != null) {
				new EpsilonTransition(last, a.start).connect();
			}
			last = a.end;
		}
		return factory.create(s, e);
	}

	public NFAComponent matchEmpty() {
		State s = new State();
		return factory.create(s, s);
	}

	public NFAComponent matchNothing() {
		State s = new State();
		return factory.create(s, null);
	}

	public NFAComponent matchOptional(NFAComponent a) {
		State s = new State();
		State e = new State();
		new EpsilonTransition(s, e).connect();
		new EpsilonTransition(s, a.start).connect();
		new EpsilonTransition(a.end, e).connect();
		return factory.create(s, e);
	}

	public NFAComponent matchUnlimitedLoop(NFAComponent a, int start) {
		if (start == 0) {
			return matchStarLoop(a);
		} else {
			List<NFAComponent> as = new ArrayList<>();
			as.addAll(copyOf(a.clone(), start));
			as.add(matchStarLoop(a));
			return matchConcatenation(as);
		}
	}

	public NFAComponent matchStarLoop(NFAComponent a) {
		State s = new State();
		State e = new State();
		new EpsilonTransition(s, a.start).connect();
		new EpsilonTransition(s, e).connect();
		new EpsilonTransition(a.end, a.start).connect();
		new EpsilonTransition(a.end, e).connect();
		return factory.create(s, e);
	}

	public NFAComponent matchRangeLoop(NFAComponent a, int start, int end) {
		if (start == end) {
			return matchFixedLoop(a, start);
		} else if (start == 0) {
			return matchUpToN(a, end);
		} else {
			NFAComponent aFixed = matchFixedLoop(a.clone(), start);
			NFAComponent aUpToN = matchUpToN(a, end - start);
			NFAComponent matchConcatenation = matchConcatenation(asList(aFixed, aUpToN));
			return matchConcatenation;
		}
	}

	public NFAComponent matchFixedLoop(NFAComponent a, int count) {
		List<NFAComponent> as = copyOf(a, count);
		return matchConcatenation(as);
	}

	public NFAComponent matchUpToN(NFAComponent a, int count) {
		State s = new State();
		State e = new State();
		new EpsilonTransition(s, e).connect();

		State current = s;
		for (int i = 0; i < count; i++) {
			NFAComponent ai = a.clone();
			new EpsilonTransition(current, ai.start).connect();
			new EpsilonTransition(ai.end, e).connect();
			current = ai.end;
		}
		return factory.create(s, e);
	}

	private static List<NFAComponent> copyOf(NFAComponent a, int count) {
		List<NFAComponent> copies = new ArrayList<>(count);
		copies.add(a);
		for (int i = 1; i < count; i++) {
			copies.add(a.clone());
		}
		return copies;
	}

	@Override
	public NFAComponent visitAlternatives(AlternativesNode node) {
		List<NFAComponent> as = accept(node.getSubNodes());
		return matchAlternatives(as);
	}

	@Override
	public NFAComponent visitAnyChar(AnyCharNode node) {
		List<NFAComponent> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public NFAComponent visitCharClass(CharClassNode node) {
		List<NFAComponent> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public NFAComponent visitCompClass(CompClassNode node) {
		List<NFAComponent> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public NFAComponent visitConcat(ConcatNode node) {
		List<NFAComponent> as = accept(node.getSubNodes());
		return matchConcatenation(as);
	}

	@Override
	public NFAComponent visitEmpty(EmptyNode node) {
		return matchEmpty();
	}

	@Override
	public NFAComponent visitGroup(GroupNode node) {
		int no = nextGroupIndex();
		NFAComponent a = node.getSubNode().accept(this);
		return matchGroup(a, no);
	}

	@Override
	public NFAComponent visitBoundedLoop(BoundedLoopNode node) {
		NFAComponent a = node.getSubNode().accept(this);
		int from = node.getFrom();
		int to = node.getTo();
		return matchRangeLoop(a, from, to);
	}

	@Override
	public NFAComponent visitUnboundedLoop(UnboundedLoopNode node) {
		NFAComponent a = node.getSubNode().accept(this);
		int from = node.getFrom();
		return matchUnlimitedLoop(a, from);
	}

	@Override
	public NFAComponent visitOptional(OptionalNode node) {
		NFAComponent a = node.getSubNode().accept(this);
		return matchOptional(a);
	}

	@Override
	public NFAComponent visitRangeChar(RangeCharNode node) {
		return match(node.getFrom(), node.getTo());
	}

	@Override
	public NFAComponent visitSingleChar(SingleCharNode node) {
		return match(node.getValue());
	}

	@Override
	public NFAComponent visitSpecialCharClass(SpecialCharClassNode node) {
		List<NFAComponent> as = accept(node.toCharNodes());
		return matchAlternatives(as);
	}

	@Override
	public NFAComponent visitString(StringNode node) {
		return match(node.getValue());
	}

	private List<NFAComponent> accept(List<? extends RegexNode> nodes) {
		List<NFAComponent> as = new ArrayList<NFAComponent>(nodes.size());
		for (RegexNode node : nodes) {
			as.add(node.accept(this));
		}
		return as;
	}

	public NFA build(RegexNode node) {
		NFAComponent nfa = node.accept(this);
		return build(nfa);
	}

	public NFA build(NFAComponent nfa) {
		State start = nfa.start;
		State end = nfa.end;
		if (end != null) {
			end.setAccepting();
		}
		return new NFA(start, charset);
	}
}
