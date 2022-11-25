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
package to.etc.webapp.query;

/**
 * Thrown for all cases where a record is not found but required. Please take heed: many methods
 * return null when a record is not found instead of throwing an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 11, 2010
 */
public class QNotFoundException extends QDbException {
	/**
	 * REMOVE AS SOON AS POSSIBLE - You are NOT ALLOWED to change the CODE for a KNOWN EXCEPTION!N!#^*&#!%#*^$%@*^$^@$#^@$#^*!@$13263
	 */
	@Deprecated
	public QNotFoundException() {
		super(QMessages.recordNotFoundSimple);
	}

	/**
	 * REMOVE AS SOON AS POSSIBLE - You are NOT ALLOWED to change the CODE for a KNOWN EXCEPTION!N!#^*&#!%#*^$%@*^$^@$#^@$#^*!@$13263
	 */
	@Deprecated
	public QNotFoundException(Throwable x) {
		super(x, QMessages.recordNotFoundSimple);
	}

	public QNotFoundException(String type, Object key) {
		super(QMessages.recordNotFound, type, key);
	}

	public QNotFoundException(Class< ? > type, Object key) {
		super(QMessages.recordNotFound, type.getName(), key);
	}
}
