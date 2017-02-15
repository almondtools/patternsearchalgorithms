package net.amygdalum.patternsearchalgorithms.pattern;

import static java.util.Arrays.asList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.amygdalum.util.builders.Arrays;

public class PatternRule implements TestRule {

	private OptimizationTarget mode;
	private Charset charset;

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				List<OptimizationTarget> modes = getModes(description);
				List<Charset> charsets = getCharsets(description);
				StringBuilder failures = new StringBuilder();
				StackTraceElement[] stackTrace = null;
				for (OptimizationTarget mode : modes) {
					PatternRule.this.mode = mode;
					for (Charset charset : charsets) {
						PatternRule.this.charset = charset;
						try {
							base.evaluate();
						} catch (AssertionError e) {
							String message = e.getMessage() == null ? "" : e.getMessage();
							failures.append(computeMessage(message)).append('\n');
							if (stackTrace == null) {
								stackTrace = e.getStackTrace();
							}
						} catch (Throwable e) {
							String message = e.getMessage() == null ? "" : e.getMessage();
							throw new RuntimeException(computeMessage(message), e);
						}
					}
				}
				if (failures.length() > 0) {
					AssertionError ne = new AssertionError(failures);
					ne.setStackTrace(stackTrace);
					throw ne;
				}
			}
		};
	}

	public Pattern compile(String string, PatternOption... options) {
		Arrays<PatternOption> testedOptions = Arrays.<PatternOption> init(options.length + 1);
		testedOptions.add(mode);
		if (charset != null) {
			testedOptions.add(new CharsetOption(charset));
		}
		testedOptions.addAll(options);
		return Pattern.compile(string, testedOptions.build(new PatternOption[0]));
	}

	private String computeMessage(String message) {
		return new StringBuilder()
			.append("in mode <").append(mode).append("> ")
			.append("with charset <").append(charset).append("> ")
			.append(": ").append(message).append("\n").toString();
	}

	private List<OptimizationTarget> getModes(Description description) {
		Only only = description.getAnnotation(Only.class);
		if (only == null) {
			only = description.getTestClass().getAnnotation(Only.class);
		}
		Not not = description.getAnnotation(Not.class);
		if (not == null) {
			not = description.getTestClass().getAnnotation(Not.class);
		}
		List<OptimizationTarget> modes = new ArrayList<OptimizationTarget>();
		if (only != null) {
			modes.addAll(asList(only.value()));
		} else {
			modes.addAll(EnumSet.allOf(OptimizationTarget.class));
		}
		if (not != null) {
			modes.removeAll(asList(not.value()));
		}
		return modes;
	}

	private List<Charset> getCharsets(Description description) {
		Charsets testedCharsets = description.getAnnotation(Charsets.class);
		if (testedCharsets == null) {
			testedCharsets = description.getTestClass().getAnnotation(Charsets.class);
		}

		List<Charset> charsets = new ArrayList<Charset>();
		if (testedCharsets == null) {
			charsets.add(null);
		} else {
			for (String charset : testedCharsets.value()) {
				if (charset.equals("CHARS")) {
					charsets.add(null);
				} else {
					charsets.add(Charset.forName(charset));
				}
			}
		}
		return charsets;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface Only {
		OptimizationTarget[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface Not {
		OptimizationTarget[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface Charsets {
		String[] value();
	}
}
