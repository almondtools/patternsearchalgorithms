package net.amygdalum.patternsearchalgorithms.automaton.chars;

import static java.util.Arrays.asList;

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

public class NFABuilder implements RegexNodeVisitor<NFAComponent> {

	private int groupIndex;

	public NFABuilder() {
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
		return new NFAComponent(s, e);
	}

	public NFAComponent match(String value) {
		State s = new State();
		State e = new State();
		connect(s, e, value);
		return new NFAComponent(s, e);
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
		return new NFAComponent(s, e);
	}

	private void connect(State s, State e, String value) {
		char[] chars = value.toCharArray();
		State[] states = new State[chars.length + 1];
		states[0] = s;
		for (int i = 1; i < chars.length; i++) {
			states[i] = new State();
		}
		states[chars.length] = e;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			State state = states[i];
			State target = states[i + 1];
			new CharTransition(state, c, target).connect();
		}
	}

	private void connect(State s, State e, char value) {
		new CharTransition(s, value, e).connect();
	}

	private void connect(State s, State e, char from, char to) {
		new CharsTransition(s, from, to, e).connect();
	}

	public NFAComponent matchGroup(NFAComponent a, int no) {
		State s = new State();
		State e = new State();
		new EpsilonTransition(s, a.start).withAction(new StartGroup(no)).connect();
		new EpsilonTransition(a.end, e).withAction(new EndGroup(no)).connect();
		return new NFAComponent(s, e);
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
		return new NFAComponent(s, e);
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
		return new NFAComponent(s, e);
	}

	public NFAComponent matchEmpty() {
		State s = new State();
		return new NFAComponent(s, s);
	}

	public NFAComponent matchNothing() {
		State s = new State();
		return new NFAComponent(s, null);
	}

	public NFAComponent matchOptional(NFAComponent a) {
		State s = new State();
		State e = new State();
		new EpsilonTransition(s, e).connect();
		new EpsilonTransition(s, a.start).connect();
		new EpsilonTransition(a.end, e).connect();
		return new NFAComponent(s, e);
	}

	public NFAComponent matchUnlimitedLoop(NFAComponent a, int start) {
		if (start == 0) {
			return matchStarLoop(a);
		} else {
			List<NFAComponent> as = copyOf(a, start);
			as.add(matchStarLoop(a.clone()));
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
		return new NFAComponent(s, e);
	}

	public NFAComponent matchRangeLoop(NFAComponent a, int start, int end) {
		if (start == end) {
			return matchFixedLoop(a, start);
		} else if (start == 0) {
			return matchUpToN(a, end);
		} else {
			NFAComponent aFixed = matchFixedLoop(a, start);
			NFAComponent aUpToN = matchUpToN(a.clone(), end - start);
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
		return new NFAComponent(s, e);
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
		return new NFA(start);
	}
}
