package net.amygdalum.patternsearchalgorithms.pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class OptimizationTargetRule implements TestRule {

	protected OptimizationTarget mode;

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				List<OptimizationTarget> modes = getModes(description);
				Map<OptimizationTarget, String> failures = new EnumMap<OptimizationTarget, String>(OptimizationTarget.class);
				StackTraceElement[] stackTrace = null;
				for (OptimizationTarget mode : modes) {
					OptimizationTargetRule.this.mode = mode;
					try {
						base.evaluate();
					} catch (AssertionError e) {
						String message = e.getMessage() == null ? "" : e.getMessage();
						failures.put(mode, message);
						if (stackTrace == null) {
							stackTrace = e.getStackTrace();
						}
					} catch (Throwable e) {
						String message = e.getMessage() == null ? "" : e.getMessage();
						throw new RuntimeException("In mode " + mode.toString() + ": " + message, e);
					}
				}
				if (!failures.isEmpty()) {
					AssertionError ne = new AssertionError(computeMessage(failures));
					ne.setStackTrace(stackTrace);
					throw ne;
				}
			}
		};
	}

	private String computeMessage(Map<OptimizationTarget, String> failures) {
		StringBuilder buffer = new StringBuilder();
		for (Map.Entry<OptimizationTarget, String> entry : failures.entrySet()) {
			buffer.append("in mode <").append(entry.getKey()).append(">: ").append(entry.getValue()).append("\n");
		}
		return buffer.toString();
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
			modes.addAll(Arrays.asList(only.value()));
		} else {
			modes.addAll(EnumSet.allOf(OptimizationTarget.class));
		}
		if (not != null) {
			modes.removeAll(Arrays.asList(not.value()));
		}
		return modes;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public @interface Only {
		OptimizationTarget[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public @interface Not {
		OptimizationTarget[] value();
	}


}
