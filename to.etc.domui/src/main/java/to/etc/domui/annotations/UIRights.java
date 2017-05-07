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

	/**
	 * If these rights depend on the data being edited, this must contain a property path expression on the
	 * <i>annotated class</i> leading to the data item to use for the check.
	 * @return
	 */
	String dataPath() default "";
}
