/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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
@Target(value = ElementType.ANNOTATION_TYPE)
public @interface MetaSearchItem {
	/**
	 * The name of the property to search on in this object. When unset this specification <i>must</i> specify
	 * a lookup field generator class.
	 * @return
	 */
	String name();

	/**
	 * If this specification is used in combination with @SearchProperty annotations this
	 * field must be used to define an order.
	 * @return
	 */
	int order() default -1;

	/**
	 * This defines the minimal length a user must enter into a search control before it
	 * is allowed to search. This can be used to prevent searches on 'a%' if that would
	 * cause a problematic query.
	 * @return
	 */
	int minLength() default -1;

	/**
	 * Generate a CI query by default. Unused?
	 * @return
	 */
	boolean ignoreCase() default true;

	/**
	 * This defines a key in the class's bundle for a string to use as the lookup field's label. This is normally used for
	 * compound specs only.
	 */
	String lookupLabelKey() default "";

	String lookupHintKey() default "";

	/**
	 * This defines how search property would be used.
	 * By default it is set to use only see {@link SearchPropertyType#SEARCH_FIELD}.
	 * This is normally used for compound specs only.
	 */
	SearchPropertyType searchType() default SearchPropertyType.SEARCH_FIELD;

	/**
	 * When T, and when search field is resolved as lookup type of control, lookup popup is shown with performed initial search automatically.
	 */
	boolean popupSearchImmediately() default false;

	/**
	 * When T, and when search field is resolved as lookup type of control, lookup popup is shown with initially collapsed search options on it's LookupForm.
	 */
	boolean popupInitiallyCollapsed() default false;
}
