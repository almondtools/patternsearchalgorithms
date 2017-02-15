package net.amygdalum.patternsearchalgorithms.automaton.chars;

import java.util.List;

public interface CharClassMapper {

	int getIndex(char ch);

	int indexCount();

	char representative(int i);

	String representatives(List<Integer> path);

}
