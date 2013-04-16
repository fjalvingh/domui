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

/**
 * Represents a single selected column.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
final public class QSelectionColumn extends QNodeBase {
	/** When used in a restriction or order an alias is needed for complex query parts. */
	@Nullable
	final private String m_alias;

	@Nonnull
	final private QSelectionItem m_item;

	protected QSelectionColumn(@Nonnull QSelectionItem item) {
		m_item = item;
		m_alias = null;
	}

	protected QSelectionColumn(@Nonnull QSelectionItem item, @Nullable String alias) {
		m_item = item;
		m_alias = alias;
	}

	/**
	 * Return the alias applied to this selection column.
	 * @return
	 */
	@Nullable
	public String getAlias() {
		return m_alias;
	}

	@Nonnull
	public QSelectionItem getItem() {
		return m_item;
	}

	@Override
	public void visit(@Nonnull QNodeVisitor v) throws Exception {
		v.visitSelectionColumn(this);
	}
}
