package to.etc.domui.component.meta;

import java.lang.annotation.*;

/**
 * This is an item in an object's default search definition. This defines
 * a property on that object as a property which the user needs to be able
 * to search on.
 * It is valid within a @Search class-level annotation only.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaSearchItem {
	/**
	 * The name of the property to search on in this object. When unset this specification <i>must</i> specify
	 * a lookup field generator class.
	 * @return
	 */
	public String name() default "";

	/**
	 * If this specification is used in combination with @SearchProperty annotations this
	 * field must be used to define an order.
	 * @return
	 */
	public int order() default -1;

	/**
	 * This defines the minimal length a user must enter into a search control before it
	 * is allowed to search. This can be used to prevent searches on 'a%' if that would
	 * cause a problematic query.
	 * @return
	 */
	public int minLength() default -1;

	/**
	 * Generate a CI query by default. Unused?
	 * @return
	 */
	public boolean ignoreCase() default true;

	/**
	 * This defines a key in the class's bundle for a string to use as the lookup field's label. This is normally used for
	 * compound specs only.
	 */
	public String lookupLabelKey() default "";

	public String lookupHintKey() default "";

	/**
	 * This defines how search property would be used.
	 * By default it is set to use only see {@link SearchPropertyType#SEARCH_FIELD}.
	 * This is normally used for compound specs only.
	 */
	public SearchPropertyType[] searchType() default {SearchPropertyType.SEARCH_FIELD};
}
