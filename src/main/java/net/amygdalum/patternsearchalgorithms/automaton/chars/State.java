package net.amygdalum.patternsearchalgorithms.automaton.chars;

import java.util.ArrayList;
import java.util.List;

public class State implements Cloneable, Comparable<State> {

	private int id;
	private List<Transition> out;
	private List<Transition> in;
	private boolean accept;
	private boolean silent;

	public State() {
		id = System.identityHashCode(this);
		out = new ArrayList<>();
		in = new ArrayList<>();
	}

	public State(int transitionCount) {
		id = System.identityHashCode(this);
		out = new ArrayList<>(transitionCount);
		in = new ArrayList<>();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setAccepting() {
		this.accept = true;
	}

	public void setAccepting(boolean accept) {
		this.accept = accept;
	}

	public boolean isAccepting() {
		return accept;
	}

	public void setSilent() {
		this.silent = true;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isSilent() {
		return silent;
	}

	public State asPrototype() {
		return new State(out.size());
	}

	void addOut(Transition transition) {
		out.add(transition);
	}

	void removeOut(Transition transition) {
		out.remove(transition);
	}

	public List<Transition> out() {
		return out;
	}

	void addIn(Transition transition) {
		in.add(transition);
	}

	void removeIn(Transition transition) {
		in.remove(transition);
	}

	public List<Transition> in() {
		return in;
	}

	public void disconnect() {
		List<Transition> inOld = in;
		in = new ArrayList<>();
		for (Transition t : inOld) {
			t.remove();
		}
		List<Transition> outOld = out;
		out = new ArrayList<>();
		for (Transition t : outOld) {
			t.remove();
		}
	}

	@Override
	public int compareTo(State that) {
		return Integer.compare(this.id, that.id);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getId()).append(" {\n");
		for (Transition transition : out) {
			buffer.append(transition.toString()).append('\n');
		}
		buffer.append('}');
		return buffer.toString();
	}

}
