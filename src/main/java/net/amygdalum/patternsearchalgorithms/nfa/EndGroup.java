package net.amygdalum.patternsearchalgorithms.nfa;

public class EndGroup implements Action {

	private int no;

	public EndGroup(int no) {
		this.no = no;
	}

	@Override
	public Groups applyTo(Groups groups, long pos) {
		return groups.endGroup(no, pos);
	}

	@Override
	public String toString() {
		return "\u2193" + no;
	}

}
