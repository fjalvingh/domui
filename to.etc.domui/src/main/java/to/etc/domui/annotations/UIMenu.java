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
package to.etc.domui.annotations;

import java.lang.annotation.*;

/**
 * When present on an UrlPage, this defines the basic data that is needed to have the page be
 * accessible from a menu. This can be used to maintain menu data close to a page. All texts
 * accessed herein
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIMenu {
	/** Used to override the default location for the message file. The class from where we lookup the bundle by name */
	Class< ? > bundleBase() default Object.class;

	/** The name of the message bundle defining the texts for this entry. When it is needed but not set the name defaults to "messages". */
	String bundleName() default "";

	/** The base for a key to use for finding the subitems; this base is used and .title, .label, .search gets added to it. */
	String baseKey() default "";

	/** The unique ID for this page in the menu. If not present the page name will be used as a menu ID. */
	String menuID() default "";

	/** The bundle key for the page title. This decides the page title in the PageBar, and if labelKey is empty it also defines the menu's label. */
	String titleKey() default "";

	/** Defines the bundle key for the set of keywords to search for this item. */
	String searchKey() default "";

	/** Defines the bundle key for a short description of this page, to be used in the menu. */
	String descKey() default "";

	/** The key describing the menu label for this page. */
	String labelKey() default "";

	/** The application-relative URL of the icon that should be used to show on the menu entry and the title bar. If an iconBase is present this name is treated as a classpath resource starting at the base class specified. */
	String iconName() default "";

	/** When present this indicates that the icon is a classpath resource. The name is looked up relative to this class. */
	Class< ? > iconBase() default Object.class;
}
