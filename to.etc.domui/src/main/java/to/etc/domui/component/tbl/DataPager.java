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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;

import java.util.function.Function;

/**
 * Wrapper for a "default" datapager. This wraps and proxies the
 * actual default data pager used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2019
 */
@NonNullByDefault
final public class DataPager extends Div implements IDataTableChangeListener {
	@NonNull
	static private Function<PageableTabularComponentBase<?>, IDataTablePager> m_pagerFactory = table -> new DataPager2(table);

	private final IDataTablePager m_pager;

//	public DataPager() {}

	public DataPager(final PageableTabularComponentBase< ? > tbl) {
		m_pager = getPagerFactory().apply(tbl);
	}

	@Override
	public void createContent() {
		add((NodeBase) m_pager);
	}

	@Override
	public void selectionUIChanged(@NonNull TableModelTableBase< ? > tbl) throws Exception {
		m_pager.selectionUIChanged(tbl);
	}

//	public void addButton(IIconRef image, final IClicked<DataPager> click, BundleRef bundle, final String ttlkey) {
//		SmallImgButton i = new SmallImgButton(image, (IClicked<SmallImgButton>) b -> click.clicked(DataPager.this));
//		if(bundle != null)
//			i.setTitle(bundle.getString(ttlkey));
//		else if(ttlkey != null)
//			i.setTitle(ttlkey);
//		getButtonDiv().add(i);
//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	DataTableChangeListener implementation.				*/
	/*--------------------------------------------------------------*/
	@Override
	public void modelChanged(@NonNull TableModelTableBase< ? > tbl, @Nullable ITableModel< ? > old, @Nullable ITableModel< ? > nw) throws Exception {
		m_pager.modelChanged(tbl, old, nw);
	}

	@Override
	public void pageChanged(@NonNull TableModelTableBase< ? > tbl) throws Exception {
		m_pager.pageChanged(tbl);
	}

	public boolean isShowSelection() {
		return m_pager.isShowSelection();
	}

	public void setShowSelection(boolean showSelection) {
		m_pager.setShowSelection(showSelection);
	}

	public void addButton(@NonNull SmallImgButton sib) {
		m_pager.addButton(sib);
	}

	public SmallImgButton addButton(@NonNull IIconRef img, @NonNull IClicked<SmallImgButton> clicked) {
		SmallImgButton sib = new SmallImgButton(img, clicked);
		addButton(sib);
		return sib;
	}

	public synchronized static void setPagerFactory(@NonNull Function<PageableTabularComponentBase<?>, IDataTablePager> pagerFactory) {
		m_pagerFactory = pagerFactory;
	}

	@NonNull private synchronized static Function<PageableTabularComponentBase<?>, IDataTablePager> getPagerFactory() {
		return m_pagerFactory;
	}
}
