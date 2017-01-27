package net.amygdalum.patternsearchalgorithms.nfa;

import java.nio.charset.Charset;

public class NFA {

	private State start;
	private Charset charset;

	public NFA(State start, Charset charset) {
		this.start = start;
		this.charset = charset;
	}
	
	public State getStart() {
		return start;
	}
	
	public Charset getCharset() {
		return charset;
	}

}
