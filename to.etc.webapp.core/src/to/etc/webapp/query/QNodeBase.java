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

import javax.annotation.*;

abstract public class QNodeBase {
	abstract public void visit(@Nonnull QNodeVisitor v) throws Exception;


	/**
	 * Render all nodes using the default string renderer.
	 * @see java.lang.Object#toString()
	 */
	@Override
	@Nonnull
	public String toString() {
		QQueryRenderer qr = new QQueryRenderer();
		try {
			String cn = getClass().getName();
			cn = cn.substring(cn.lastIndexOf('.') + 1);
			qr.append(cn);
			qr.append("[");
			visit(qr);
			qr.append("]");
			return qr.toString();
		} catch(Exception x) {
			return getClass().getName() + ": exception " + x + "]";
		}
	}
}
