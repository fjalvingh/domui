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
package to.etc.domui.legacy.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.component.tbl.SimpleColumnDef;
import to.etc.domui.component.tbl.SimpleRowRenderer;

/**
 * Deprecated: the widths calculation is completely unclear (jal 2012/05/11).
 *
 * Row renderer that is used for MultipleSelectionLookup control.
 * First selection indicator column is additionaly rendered from outer code, so abstract methods that resolve selection column and total row width must be set additionaly.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2009
 */
@Deprecated
public abstract class MultipleSelectionRowRenderer<T> extends SimpleRowRenderer<T> {

	public MultipleSelectionRowRenderer(Class<T> dataClass, String[] cols) {
		super(dataClass, cols);
	}

	public MultipleSelectionRowRenderer(Class<T> dataClass) {
		super(dataClass);
	}

	public MultipleSelectionRowRenderer(@Nonnull final Class<T> dataClass, @Nonnull final ClassMetaModel cmm, final String... cols) {
		super(dataClass, cmm);
	}

	/**
	 * Initialize, using the genericized table column set. Reserve some extra space for selection indicator column that is added as first column.
	 * @param clz
	 * @param xdpl
	 */
	@Override
	protected void initialize(@Nonnull final List<ExpandedDisplayProperty< ? >> xdpl) {
		//-- For all properties in the list, use metadata to define'm
		final int[] widths = new int[80];
		setTotalWidth(0);
		int ix = 0;
		addColumns(xdpl, widths);
		ix = 0;
		int remainingWidthInPixels = getRowWidth() - getSelectionColWidth();
		for(final SimpleColumnDef< ? > scd : getColumnList()) {
			/*
			 * Calculation:
			 *
			 *        width[ix]     remainingWidthInPixels
			 * 100% * ----------- * ----------------------
			 *        totalWidth    totalWidthInPixels
			 *
			 */
			// FIXME Explain/fix calculation (2012/05/11 jal)
			final int pct = (100 * widths[ix++] * remainingWidthInPixels) / (getTotalWidth() * getRowWidth());
			scd.setWidth(pct + "%");
		}
	}

	public abstract int getRowWidth();

	public abstract int getSelectionColWidth();
}
