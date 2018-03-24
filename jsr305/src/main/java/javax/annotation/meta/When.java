/*
 * DomUI Java User Interface - shared code
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
package javax.annotation.meta;

/**
 * Used to describe the relationship between a qualifier T and the set of values
 * S possible on an annotated element.
 *
 * In particular, an issues should be reported if an ALWAYS or MAYBE value is
 * used where a NEVER value is required, or if a NEVER or MAYBE value is used
 * where an ALWAYS value is required.
 *
 *
 */
public enum When {
    /** S is a subset of T */
    ALWAYS,
    /** nothing definitive is known about the relation between S and T */
    UNKNOWN,
    /** S intersection T is non empty and S - T is nonempty */
    MAYBE,
    /** S intersection T is empty */
    NEVER

}
