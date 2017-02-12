package net.amygdalum.patternsearchalgorithms.pattern;

public interface Matcher {

	boolean matches();

	boolean prefixes();

	boolean find();

	long start();
	long start(int no);

	long end();
	long end(int no);

	String group();
	String group(int no);

}
