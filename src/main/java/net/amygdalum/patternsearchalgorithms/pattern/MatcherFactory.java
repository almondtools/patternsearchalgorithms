package net.amygdalum.patternsearchalgorithms.pattern;

import net.amygdalum.util.io.ByteProvider;

public interface MatcherFactory {

	Matcher newMatcher(ByteProvider input);

}
