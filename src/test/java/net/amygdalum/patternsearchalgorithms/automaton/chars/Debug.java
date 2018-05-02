package net.amygdalum.patternsearchalgorithms.automaton.chars;

import java.io.FileOutputStream;
import java.io.IOException;


public final class Debug {

	public static void export(NFA nfa, String name) {
		try {
			new NFAExport(nfa, name).to(new FileOutputStream(name + ".dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
