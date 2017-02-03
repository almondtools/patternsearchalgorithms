package net.amygdalum.patternsearchalgorithms.pattern;

import java.util.List;

public interface Matcher {

	boolean matches();

	boolean prefixes();

	boolean find();

	long start();

	long end();

	String group();

	List<String> groups();

}
