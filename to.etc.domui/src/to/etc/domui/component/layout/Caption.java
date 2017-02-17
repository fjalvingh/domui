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
package to.etc.domui.component.layout;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

import java.util.*;

public class Caption extends Div {

	private static final String FIRST_CAPTION = "ui-cptn-first";

	private String m_caption;

	private TD m_buttonpart;

	private List<SmallImgButton> m_btns = Collections.EMPTY_LIST;

	private Table m_table;

	private TD m_ttltd;

	private Img m_icon;

	private Div m_ttldiv;

	public Caption() {}

	public Caption(String ttl) {
		m_caption = ttl;
	}

	public Caption(String ttl, boolean isFirst) {
		this(ttl);
		setFirst(isFirst);
	}

	public String getCaption() {
		return m_caption;
	}

	public void setCaption(String caption) {
		if(DomUtil.isEqual(m_caption, caption))
			return;
		m_caption = caption;
		forceRebuild();
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-cptn");
		m_table = new Table();
		add(m_table);
		m_table.setCellPadding("0");
		m_table.setCellSpacing("0");
		m_table.setTableWidth("100%");
		TBody b = new TBody();
		m_table.add(b);
		m_ttltd = b.addRowAndCell();
		m_ttltd.setCssClass("ui-cptn-ttl");
		m_ttltd.setNowrap(true);
		m_ttldiv = new Div();
		m_ttltd.add(m_ttldiv);

		//		ttl.setCssClass("ui-cptn-ttl");
		m_ttldiv.add(m_caption);
		TD right = b.addCell();
		right.setCssClass("ui-cptn-btn");
		m_buttonpart = right;
		right.setAlign(TDAlignType.RIGHT);

		if(m_icon != null)
			m_buttonpart.add(m_icon);

		for(SmallImgButton btn : m_btns) {
			m_buttonpart.add(btn);
		}
	}

	public void addIcon(String src) {
		if(m_icon == null) {
			m_icon = new Img(src);
			if(isBuilt())
				m_buttonpart.add(0, m_icon);
		} else
			m_icon.setSrc(src);
		m_icon.setAlign(ImgAlign.RIGHT);
	}

	public void addButton(String image, String hint, IClicked<NodeBase> handler) {
		SmallImgButton ib = new SmallImgButton(image);
		ib.setClicked(handler);
		internallyAddButton(ib, hint);
	}

	public void addButton(String image, String hint, String onClickJs) {
		SmallImgButton ib = new SmallImgButton(image);
		ib.setOnClickJS(onClickJs);
		internallyAddButton(ib, hint);
	}

	private void internallyAddButton(SmallImgButton ib, String hint) {
		if(m_btns == Collections.EMPTY_LIST) {
			m_btns = new ArrayList<SmallImgButton>();
		}
		ib.setTitle(hint);
		m_btns.add(ib);
		if(isBuilt() && m_buttonpart != null) {
			m_buttonpart.add(ib);
		}
	}

	public void setFirst(boolean isFirst) {
		if(isFirst) {
			addCssClass(FIRST_CAPTION);
		} else {
			removeCssClass(FIRST_CAPTION);
		}
	}

	public boolean isFirst() {
		return hasCssClass(FIRST_CAPTION);
	}
}
