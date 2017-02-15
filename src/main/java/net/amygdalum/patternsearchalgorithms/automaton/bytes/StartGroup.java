package net.amygdalum.patternsearchalgorithms.automaton.bytes;

public class StartGroup implements Action {

	private int no;

	public StartGroup(int no) {
		this.no = no;
	}

	@Override
	public Groups applyTo(Groups groups, long pos) {
		return groups.startGroup(no, pos);
	}
	
	@Override
	public String toString() {
		return "\u2191" + no;
	}

}
