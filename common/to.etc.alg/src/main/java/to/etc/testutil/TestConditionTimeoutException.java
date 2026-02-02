package to.etc.testutil;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;

@NonNullByDefault
final public class TestConditionTimeoutException extends RuntimeException {
	public TestConditionTimeoutException(Duration timeout, ArrayList<TestCondition> unresolved) {
		super("Test conditions " + unresolved.stream().map(x-> "'".concat(x.getName()).concat("'")).collect(Collectors.joining(", ")) + " failed to complete within " + timeout);
	}
}
