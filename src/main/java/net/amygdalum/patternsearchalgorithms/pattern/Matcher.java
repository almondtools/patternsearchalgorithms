package net.amygdalum.patternsearchalgorithms.pattern;

public interface Matcher {

	boolean matches();

	boolean find();

	long start();

	long end();

	String group();

}
