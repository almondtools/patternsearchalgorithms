package net.amygdalum.patternsearchalgorithms.export;

import static net.amygdalum.util.text.ByteEncoding.decode;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import net.amygdalum.patternsearchalgorithms.nfa.EpsilonTransition;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;
import net.amygdalum.patternsearchalgorithms.nfa.OrdinaryTransition;
import net.amygdalum.patternsearchalgorithms.nfa.State;

public class NFAExport {

	private NFA automaton;
	private String name;

	public NFAExport(NFA automaton, String name) {
		this.automaton = automaton;
		this.name = name;
	}

	public void to(OutputStream out) throws IOException {
		OutputStreamWriter w = null;
		try {
			w = new OutputStreamWriter(out, "UTF-8");
			w.write("digraph \"" + name + "\" {\n");
			writeStart(w);
			writeAutomaton(w);
			w.write("}");
		} finally {
			if (w != null) {
				w.close();
			}
		}
	}

	private void writeStart(OutputStreamWriter writer) throws IOException {
		writer.write("start [shape=point];\n");
		writer.write("start -> " + automaton.getStart().getId() + ";\n");
	}

	public void writeAutomaton(Writer writer) throws IOException {
		for (State state : automaton.states()) {
			writeState(writer, state);
			for (EpsilonTransition epsilon : state.epsilons()) {
				writeTransition(writer, epsilon);
			}
			for (OrdinaryTransition ordinary : state.ordinaries()) {
				writeTransition(writer, ordinary);
			}
		}
	}

	private void writeState(Writer writer, State state) throws IOException {
		int stateId = state.getId();
		if (state.isAccepting()) {
			String format = " [shape=doublecircle label=\"" + stateId + "\"]";
			writer.write(stateId + format + ";\n");
		} else {
			String format = " [shape=circle]";
			writer.write(stateId + format + ";\n");
		}
	}

	private void writeTransition(Writer writer, OrdinaryTransition transition) throws IOException {
		byte from = transition.getFrom();
		byte to = transition.getTo();
		Charset charset = automaton.getCharset();
		String format = (from == to) ? " [label=\"" + decode(charset, from) + "\"]" : " [label=\"" + decode(charset, from) + "-" + decode(charset, to) + "\"]";
		writer.write(transition.getOrigin().getId() + " -> " + transition.getTarget().getId() + format + ";\n");
	}

	private void writeTransition(Writer writer, EpsilonTransition transition) throws IOException {
		String format = " [label=\"&epsilon;\"]";
		writer.write(transition.getOrigin().getId() + " -> " + transition.getTarget().getId() + format + ";\n");
	}

}
