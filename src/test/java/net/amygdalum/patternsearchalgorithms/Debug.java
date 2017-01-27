package net.amygdalum.patternsearchalgorithms;

import java.io.FileOutputStream;
import java.io.IOException;

import net.amygdalum.patternsearchalgorithms.export.NFAExport;
import net.amygdalum.patternsearchalgorithms.nfa.NFA;

public final class Debug {

	public static void export(NFA nfa, String name) {
		try {
			new NFAExport(nfa, name).to(new FileOutputStream(name + ".dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
