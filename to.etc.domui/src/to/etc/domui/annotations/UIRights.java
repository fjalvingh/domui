package to.etc.domui.annotations;

import java.lang.annotation.*;

/**
 * Defines the rights that are required for a page to be usable by a user. When this annotation
 * is present on a page the page can be accessed by a user which has the prerequisite rights. If
 * the user is not currently known (meaning he has not logged in yet) this causes the page logic
 * to throw a NotLoggedInException, which in turn should force a login to occur.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2009
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIRights {
	/**
	 * The rights that the user must have to access the page. If multiple entries are set it means ALL rights
	 * specified must be present to allow access.
	 * @return
	 */
	String[] value() default {};
}
