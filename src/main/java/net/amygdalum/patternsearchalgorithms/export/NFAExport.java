package net.amygdalum.patternsearchalgorithms.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import net.amygdalum.patternsearchalgorithms.automaton.bytes.EpsilonTransition;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.NFA;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.OrdinaryTransition;
import net.amygdalum.patternsearchalgorithms.automaton.bytes.State;
import net.amygdalum.util.text.ByteEncoding;

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
		String action = transition.getAction() == null ? "" : "/" + transition.getAction().toString();
		Charset charset = automaton.getCharset();
		String format = (from == to) ? " [label=\"" + decode(charset, from) + action + "\"]" : " [label=\"" + decode(charset, from) + "-" + decode(charset, to) + action + "\"]";
		writer.write(transition.getOrigin().getId() + " -> " + transition.getTarget().getId() + format + ";\n");
	}

	private void writeTransition(Writer writer, EpsilonTransition transition) throws IOException {
		String action = transition.getAction() == null ? "" : "/" + transition.getAction().toString();
		String format = " [label=\"&epsilon;" + action + "\"]";
		writer.write(transition.getOrigin().getId() + " -> " + transition.getTarget().getId() + format + ";\n");
	}

	private String decode(Charset charset, byte value) {
		String decoded = "#" + String.format("%02X ", value);
		char[] chars = ByteEncoding.decode(charset, value).toCharArray();
		if (chars.length == 1 && Character.isDefined(chars[0]) && chars[0] >= 32 && chars[0] < 128) {
			decoded = String.valueOf(chars[0]);
		}
		return decoded;
	}

}
