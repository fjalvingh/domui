package to.etc.testutil;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 04-01-21.
 */
final public class TestConditionFailedException extends RuntimeException {
	public TestConditionFailedException(String message) {
		super(message);
	}
}
