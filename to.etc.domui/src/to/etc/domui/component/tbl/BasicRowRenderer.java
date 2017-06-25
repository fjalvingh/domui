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

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * Highly customizable RowRenderer. This has many ways to customize the row output, often using
 * metadata. The definition for this renderer can be set until it's first use; it's actual definition
 * gets calculated at the time it's first used.
 * The possible specifications used in property modifiers are:
 * <ul>
 *	<li>"%28": a String starting with % denotes a width in percents. %28 gets translated to setWidth("28%");</li>
 *	<li>"^Title": a String starting with ^ denotes the header caption to use. Use ^~key to internationalize.</li>
 *	<li>"$cssclass": a String denoting a CSS class.</li>
 *	<li>Class&lt;? extends IConverter&gt;: the converter to use to convert the value to a string</li>
 *	<li>IConverter: an instance of a converter</li>
 *	<li>Class&lt;? extends INodeContentRenderer&lt;T&gt;&gt;: the class to use to render the content of the column.</li>
 *	<li>INodeContentRenderer&lt;T&gt;: an instance of a node renderer to use to render the content of the column.</li>
 *	<li>BasicRowRenderer.NOWRAP: forces a 'nowrap' on the column</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
public class BasicRowRenderer<T> extends AbstractRowRenderer<T> implements IClickableRowRenderer<T> {
	static public final String NOWRAP = SimpleColumnDef.NOWRAP;

	static public final String WRAP = SimpleColumnDef.WRAP;

	static public final String DEFAULTSORT = SimpleColumnDef.DEFAULTSORT;

	static public final String NUMERIC = "$ui-numeric";

	/** The column name to sort on by default, set by metadata. This is only used to keep it for a while until the actual column list is known; at that point the column def to sort on is determined and used. */
	private String m_sortColumnName;

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public BasicRowRenderer(@Nonnull final Class<T> dataClass, final Object... cols) throws Exception {
		super(dataClass);
		initColumnSet(cols);
	}

	public BasicRowRenderer(@Nonnull final Class<T> dataClass, @Nonnull final ClassMetaModel cmm, final Object... cols) {
		super(dataClass, cmm);
		initColumnSet(cols);
	}

	private void initColumnSet(Object[] cols) {
		if(cols.length != 0)
			addColumns(cols);
		m_sortColumnName = model().getDefaultSortProperty();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Add the specified list of property names and presentation options to the column definitions. The items passed in the
	 * columns object can be multiple property definitions followed by specifications. A property name is a string starting
	 * with a letter always. All other Strings and objects are treated as specifications for display. The possible specifications
	 * are:
	 * <ul>
	 *	<li>"%28": a String starting with % denotes a width in percents. %28 gets translated to setWidth("28%");</li>
	 *	<li>"^Title": a String starting with ^ denotes the header caption to use. Use ^~key~ to internationalize.</li>
	 *	<li>"$cssclass": a String denoting a CSS class.</li>
	 *	<li>Class&lt;? extends IConverter&gt;: the converter to use to convert the value to a string</li>
	 *	<li>IConverter: an instance of a converter</li>
	 *	<li>Class&lt;? extends INodeContentRenderer&lt;T&gt;&gt;: the class to use to render the content of the column.</li>
	 *	<li>INodeContentRenderer&lt;T&gt;: an instance of a node renderer to use to render the content of the column.</li>
	 *	<li>BasicRowRenderer.NOWRAP: forces a 'nowrap' on the column</li>
	 * </ul>
	 *
	 * @param clz
	 * @param cols
	 * <X, C extends IConverter<X>, R extends INodeContentRenderer<X>>
	 */
	public <R> BasicRowRenderer<T> addColumns(@Nonnull final Object... cols) {
		getColumnList().addColumns(cols);					// Delegate to column list.
		return this;
	}

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		getColumnList().addDefaultColumns();
	}

	/**
	 * Complete this object if it is not already complete.
	 */
	@Override
	protected void complete(@Nonnull final TableModelTableBase<T> tbl) {
		if(isComplete())
			return;

		//-- If we have no columns at all we use a default column list.
		if(getColumnList().size() == 0)
			addDefaultColumns();

		//-- Is there a default sort thingy? Is that column present?
		if(m_sortColumnName != null && null == getColumnList().getSortColumn())
			getColumnList().setDefaultSortColumn(m_sortColumnName);

		getColumnList().assignPercentages();				// Calculate widths
		super.complete(tbl);
	}
}
