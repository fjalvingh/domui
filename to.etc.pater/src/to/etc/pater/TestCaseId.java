package to.etc.pater;

import java.lang.annotation.*;

/**
 * Annotation to describe the test case id for test case methods.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 11 Nov 2009
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCaseId {
	String value();
}
