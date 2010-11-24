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

import to.etc.domui.util.*;

/**
 * Valid for a page property, this defines an URL parameter that is to be filled
 * in when the page is accessed. When the page is constructed the framework will
 * scan for properties having this annotation. It will then inject the value obtained
 * from the URL parameter specified in this property into the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2008
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIUrlParameter {
	/**
	 * The name of the URL parameter to look for. If this is not present we use the property's name
	 * as the name of the URL parameter.
	 * @return
	 */
	String name() default Constants.NONE;

	/**
	 * By default all parameters defined are mandatory. Set this to false to make the URL parameter an optional
	 * value. When a value is optional it's setter is NOT called when the URL parameter is not present.
	 * @return
	 */
	boolean mandatory() default true;

	/**
	 * When set this defines that the given parameter is the primary key for this entity.
	 * @return
	 */
	Class< ? > entity() default Object.class;
}
