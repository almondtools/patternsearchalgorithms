package net.amygdalum.patternsearchalgorithms.pattern.chars;

import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.util.io.CharProvider;

public interface MatcherFactory {

	Matcher newMatcher(CharProvider input);

}
