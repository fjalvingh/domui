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
package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;

/**
 * Please use {@link BasicRowRenderer} instead.
 *
 * Renders rows from a datamodel; this tries to use the metadata for all
 * parts not explicitly specified.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
@Deprecated
public class SimpleRowRenderer<T> extends AbstractRowRenderer<T> implements IClickableRowRenderer<T> {
	private int m_totwidth;

	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public SimpleRowRenderer(@Nonnull final Class<T> dataClass, final String... cols) {
		super(dataClass);
		initColumnList(cols);
	}

	public SimpleRowRenderer(@Nonnull final Class<T> dataClass, @Nonnull final ClassMetaModel cmm, final String... cols) {
		super(dataClass, cmm);
		initColumnList(cols);
	}

	private void initColumnList(@Nonnull String[] cols) {
		if(cols.length != 0)
			initializeExplicitColumns(cols);
		else
			initializeDefaultColumns();

		//-- Is there a default sort thingy? Is that column present?
		final String sort = model().getDefaultSortProperty();
		if(null != sort)
			getColumnList().setDefaultSortColumn(sort);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * This initializes the ColumnList by auto-decoding all required data from the class and the
	 * list of columns specified. It uses metamodel info if present.
	 *
	 * @param clz
	 * @param cols
	 */
	protected void initializeExplicitColumns(final String[] cols) {
		if(cols == null || cols.length == 0)
			throw new IllegalStateException("The list-of-columns is empty or null; I need at least one column to continue.");

		final List<ExpandedDisplayProperty< ? >> xdpl = ExpandedDisplayProperty.expandProperties(model(), cols);
		initialize(xdpl);
	}

	/**
	 * Initialize, using the genericized table column set.
	 * FIXME Should be private
	 * @param clz
	 * @param xdpl
	 */
	protected void initialize(@Nonnull final List<ExpandedDisplayProperty< ? >> xdpl) {
		//-- For all properties in the list, use metadata to define'm
		final int[] widths = new int[80];
		m_totwidth = 0;
		int ix = 0;
		addColumns(xdpl, widths);
		ix = 0;
		for(final SimpleColumnDef< ? > scd : getColumnList()) {
			final int pct = (100 * widths[ix++]) / m_totwidth;
			scd.setWidth(pct + "%");
		}
	}

	/**
	 * FIXME Should be private
	 * @param xdpl
	 * @param widths
	 */
	protected void addColumns(final List<ExpandedDisplayProperty< ? >> xdpl, final int[] widths) {
		for(final ExpandedDisplayProperty< ? > xdp : xdpl) {
			if(xdp instanceof ExpandedDisplayPropertyList) {
				//-- Flatten: call for subs recursively.
				final ExpandedDisplayPropertyList<?> xdl = (ExpandedDisplayPropertyList<?>) xdp;
				addColumns(xdl.getChildren(), widths);
				continue;
			}

			//-- Create a column def from the metadata
			final SimpleColumnDef< ? > scd = createDef(xdp);
			int dl = xdp.getDisplayLength();
			if(dl <= 5)
				dl = 5;
			if(dl > 40) {
				dl = 40;
			}
			//			System.out.println("XDPL: property " + xdp.getName() + " size=" + dl);
			widths[getColumnList().size()] = dl;
			m_totwidth += dl;
			getColumnList().add(scd); // ORDER!

			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
				scd.setHeaderCssClass("ui-numeric");
			}
		}
	}

	@Nonnull
	private <V> SimpleColumnDef<V> createDef(@Nonnull ExpandedDisplayProperty<V> v) {
		return new SimpleColumnDef<V>(getColumnList(), v);
	}

	/**
	 * Called for an empty column list, this uses table metadata to create a column list. If
	 * the metadata does not contain stuff this aborts.
	 * @param clz
	 */
	private void initializeDefaultColumns() {
		final List<DisplayPropertyMetaModel> dpl = model().getTableDisplayProperties();
		if(dpl.size() == 0)
			throw new IllegalStateException("The list-of-columns to show is empty, and the class has no metadata (@MetaObject) defining a set of columns as default table columns, so there.");
		final List<ExpandedDisplayProperty< ? >> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, model(), null);
		initialize(xdpl);
	}

	protected void setTotalWidth(int w) {
		m_totwidth = w;
	}

	protected int getTotalWidth() {
		return m_totwidth;
	}

}
