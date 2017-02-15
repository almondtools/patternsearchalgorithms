package net.amygdalum.patternsearchalgorithms.pattern.bytes;

import net.amygdalum.patternsearchalgorithms.pattern.Matcher;
import net.amygdalum.util.io.ByteProvider;

public interface MatcherFactory {

	Matcher newMatcher(ByteProvider input);

}
