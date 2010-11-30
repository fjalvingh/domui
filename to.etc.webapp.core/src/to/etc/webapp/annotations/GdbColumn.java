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
package to.etc.webapp.annotations;

import java.lang.annotation.*;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 28, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface GdbColumn {
	/** Sets the base name of this column. If a table prefix is defined it is used in addition to form the full name. */
	String name() default "";

	/** Sets the full name of this column. If this is set any table prefix is ignored. */
	String fullName() default "";

	String type() default "";

	/** The length of a column. For strings this is the max size <i>in characters</i>, for numerics this is the precision. */
	int length() default -1;

	/** For numerics, the scale field. */
	int scale() default -1;

	/** Whether the field can be nullable. Defaults to false. */
	boolean nullable() default false;

	boolean unique() default false;

	/** Whether a Date column contains a timestamp, a single date or what. */
	GdbDate temporal() default GdbDate.TIMESTAMP;

	GdbEnum enumerated() default GdbEnum.STRING;

	GdbBool bool() default GdbBool.NATIVE;
}
