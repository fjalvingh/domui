package to.etc.pater;

import java.lang.annotation.*;

/**
 * This annotation sets options for the Puzzler's JUnit test runner for those tests
 * that are in trouble when running parallel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 16, 2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestConfig {
	/**
	 * Define one or more group names that this test class belongs to. These group names
	 * @return
	 */
	String[] groups() default {};

	/**
	 * Define a set of test class names and/or "parallel" group names that this test cannot run
	 * in parallel with. For test class names the mechanism is simple: this test will not run
	 * in parallel with that other test class. Alternatively you add just names here which are
	 * "group" names of tests that should not run together, like "database". For such a name, if
	 * any other test is running also with that "name" this test will wait.
	 * @return
	 */
	String[] notParallelWith() default {};

	/**
	 * When T this test class runs strictly standalone, with not a single other JUnit test in
	 * parallel.
	 * @return
	 */
	boolean serial() default false;

	/**
	 * Can be used to set test IDs.
	 * @return
	 */
	String id() default "";
}
