package net.amygdalum.patternsearchalgorithms.pattern;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
		return new CharsetOption(StandardCharsets.UTF_8);
	} 
	
	public Charset getCharset() {
		return charset;
	}

}
