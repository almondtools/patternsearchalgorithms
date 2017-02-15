package net.amygdalum.patternsearchalgorithms.pattern;

import java.nio.charset.Charset;

public class CharsetOption implements PatternOption {

	private Charset charset;

	public CharsetOption(Charset charset) {
		this.charset = charset;
	}
	
	public static CharsetOption firstOf(PatternOption[] options) {
		for (PatternOption option : options) {
			if (option instanceof CharsetOption) {
				return (CharsetOption) option;
			}
		}
		return null;
	} 
	
	public Charset getCharset() {
		return charset;
	}

}
