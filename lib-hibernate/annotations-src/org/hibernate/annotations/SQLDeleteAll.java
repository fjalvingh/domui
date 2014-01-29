//$Id: SQLDeleteAll.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SqlDelete Annotation for overwriting Hibernate default DELETE ALL method
 *
 * @author László Benke
 */
@Target( {TYPE, FIELD, METHOD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface SQLDeleteAll {
	/**
	 * Procedure name or DELETE STATEMENT
	 */
	String sql();

	/**
	 * Is the statement using stored procedure or not
	 */
	boolean callable() default false;

	/**
	 * For persistence operation what style of determining results (success/failure) is to be used.
	 */
	ResultCheckStyle check() default ResultCheckStyle.NONE;
}
