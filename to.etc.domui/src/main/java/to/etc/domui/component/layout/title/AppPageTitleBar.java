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
package to.etc.domui.component.layout.title;

import to.etc.domui.annotations.UIMenu;
import to.etc.domui.component.buttons.HoverButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component.misc.OddCharacters;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.ImgAlign;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IShelvedEntry;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A page title bar. This consists of an image in the left corner, a string describing the
 * module's functionality and a set of standard buttons opening quick-access pages. This uses
 * the generic menu system code to retrieve a module name and image, if applicable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class AppPageTitleBar extends BasePageTitleBar {
	final private boolean m_catchError;

	@Nonnull
	private final Img m_img = new Img();

	private TD m_buttonpart = new TD();

	private TD m_titlePart;

	private String m_imageUrl;

	private String m_hint;

	//// LFTODO Remove this horror
	//@Deprecated
	//private IRenderInto<String> m_titleNodeRenderer;

	private IErrorFence m_errorFence;

	private ErrorMessageDiv m_errorThingy = new ErrorMessageDiv();

	private Table m_titleTable;

	private TBody m_body;

	private boolean m_showBackButton;

	public AppPageTitleBar(boolean catchError) {
		m_catchError = catchError;
	}

	public AppPageTitleBar(final String title, boolean catchError) {
		super(title);
		m_catchError = catchError;
	}

	public AppPageTitleBar(final String icon, final String title, boolean catchError) {
		super(title);
		setIcon(icon);
		m_catchError = catchError;
	}

	public boolean isCatchError() {
		return m_catchError;
	}

	public void setIcon(final String s) {
		m_imageUrl = s;
		m_img.setDisplay(s == null ? DisplayType.NONE : DisplayType.INLINE);
	}

	public String getHint() {
		return m_hint;
	}

	public void setHint(String hint) {
		m_hint = hint;
	}

	public boolean isShowBackButton() {
		return m_showBackButton;
	}

	@Nonnull
	public AppPageTitleBar setShowBackButton(boolean showBackButton) {
		m_showBackButton = showBackButton;
		return this;
	}

	@Nonnull
	public TBody getBody() {
		if(null != m_body)
			return m_body;
		throw new IllegalStateException("Body null: call after createContent");
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		m_titleTable = new Table();
		add(m_titleTable);
		m_body = new TBody();
		setCssClass("ui-atl");
		m_titleTable.add(m_body);
		m_titleTable.setCellPadding("0");
		m_titleTable.setCellSpacing("0");
		m_titleTable.setTableBorder(0);
		TR tr = m_body.addRow();
		m_body.add(tr);

		//-- Back button and image icon...
		TD td = m_body.addCell();
		if(m_showBackButton) {
			addBackOrCloseButton(td);
		}

		setIconURL();
		if(DomUtil.isBlank(m_img.getSrc())) {
			m_img.setDisplay(DisplayType.NONE);
		}
		m_img.setAlign(ImgAlign.LEFT);
		td.add(m_img);
		td.setCssClass("ui-atl-i");

		//-- Title.
		td = m_body.addCell();
		m_titlePart = td;
		td.setCssClass("ui-atl-t");
		td.setTestID("pageTitle");
		renderTitleCell();

		//-- Buttons
		m_body.row().add(m_buttonpart);
		//		td = b.addCell();
		m_buttonpart.setCssClass("ui-atl-bb");
		//		td.setWidth("1%");
		addDefaultButtons(m_buttonpart);

		if(isCatchError()) {
			int cspan = calcColSpan(m_titleTable.getBody());
			TD c = m_titleTable.getBody().addRowAndCell();
			c.add(m_errorThingy);
			c.setColspan(cspan);
		}
	}

	private void addBackOrCloseButton(@Nonnull TD td) {
		List<IShelvedEntry> ps = getPage().getConversation().getWindowSession().getShelvedPageStack();

		if(ps.size() > 1) {									// Nothing to go back to (only myself is on page) -> exit
			IShelvedEntry se = ps.get(ps.size() - 2);		// Get the page before me
			if(se.isClose()) {
				addCloseButton(td);
				return;
			}
		} else {
			addCloseButton(td);
			return;
		}
		addBackButton(td);
	}

	private void addBackButton(@Nonnull TD td) {
		HoverButton backButton = new HoverButton(Theme.APPBAR_BACK_ICON, new IClicked<HoverButton>() {
			@Override
			public void clicked(@Nonnull HoverButton clickednode) throws Exception {
				UIGoto.back();
			}
		});
		td.add(backButton);
	}

	private void addCloseButton(@Nonnull TD td) {
		HoverButton button = new HoverButton(Theme.APPBAR_CLOSE_ICON, new IClicked<HoverButton>() {
			@Override
			public void clicked(@Nonnull HoverButton clickednode) throws Exception {
				getPage().getBody().closeWindow();
			}
		});
		td.add(button);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Error panel handling.								*/
	/*--------------------------------------------------------------*/
	/**
	 * When I'm added to a page register m_errorThingy as an error listener for that page.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		if(!isCatchError())
			return;
		m_errorFence = DomUtil.getMessageFence(this);
		m_errorFence.addErrorListener(m_errorThingy);
	}

	/**
	 * When I'm removed from a page m_errorThingy may no longer handle it's errors, so remove
	 * m_errorThingy from the error listener chain.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		if(!isCatchError())
			return;
		m_errorFence.removeErrorListener(m_errorThingy);
	}

	/**
	 * Calculate the largest colspan from all rows.
	 * @param b
	 * @return
	 */
	static private int calcColSpan(TBody b) {
		int maxcol = 1;
		for(NodeBase nb : b) {
			TR row = (TR) nb;
			int thiscol = 0;
			for(NodeBase ntd : row) {
				TD td = (TD) ntd;
				if(td.getColspan() <= 1)
					thiscol++;
				else
					thiscol += td.getColspan();
			}
			if(thiscol > maxcol)
				maxcol = thiscol;
		}

		return maxcol;
	}

	public TD getButtonpart() {
		return m_buttonpart;
	}

	/**
	 * Calculate the image URL to use for the icon.
	 * @return
	 */
	private void setIconURL() {
		if(m_imageUrl != null) { // Set by user?
			m_img.setSrc(m_imageUrl);
			return;
		}

		//-- 1. Is an icon or icon resource specified in any attached UIMenu annotation? If so use that;
		Class< ? extends UrlPage> clz = getPage().getBody().getClass();
		UIMenu ma = clz.getAnnotation(UIMenu.class);
		if(ma != null) {
			if(ma.iconName() != null) {
				if(ma.iconBase() != Object.class)
					m_img.setSrc(ma.iconBase(), ma.iconName()); // Set class-based URL
				else
					m_img.setSrc(ma.iconName()); // Set specific thingy,
			}
		}

		//-- Not set using a UIMenu annotation. Is a .png with the same classname available?
		String cn = DomUtil.getClassNameOnly(clz) + ".png";
		if(DomUtil.hasResource(clz, cn)) {
			m_img.setSrc(clz, cn);
			return;
		}

		//-- Try to get an URL from the class-based resources. FIXME Todo
		String def = getDefaultIcon();
		if(def == null) {
			return;
		}
		m_img.setSrc(def);
	}

	public String getDefaultIcon() {
		return null;
	}

	@Override
	public void setPageTitle(String ttl) {
		if(DomUtil.isEqual(getPageTitle(), ttl))
			return;

		super.setPageTitle(ttl);
		if(isBuilt()) {
			renderTitleCell();
		}
	}

	public void addButton(String image, String hint, IClicked<NodeBase> handler) {
		SmallImgButton ib = new SmallImgButton(image);
		getButtonpart().add(ib);
		ib.setTitle(hint);
		ib.setClicked(handler);
	}

	protected void addDefaultButtons(final NodeContainer nc) {
		SmallImgButton ib = new SmallImgButton("THEME/btnSpecialChar.png");
		nc.add(ib);
		ib.setTitle("Toon lijst van bijzondere tekens");
		ib.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(final @Nonnull NodeBase b) throws Exception {
				OddCharacters oc = new OddCharacters();
				getPage().getBody().add(oc);
			}
		});
	}

	public TD getTitlePart() {
		return m_titlePart;
	}

	@Override
	public void setShowAsModified(boolean showAsModified) {
		if(super.isShowAsModified() != showAsModified) {
			super.setShowAsModified(showAsModified);
			if(isBuilt()) {
				renderTitleCell();
			}
		}
	}

	private void renderTitleCell() {
		TD titleCell = getTitlePart();
		//if(m_titleNodeRenderer != null) {
		//	try {
		//		m_titleNodeRenderer.render(titleCell, getPageTitle()); //, Boolean.valueOf(isShowAsModified()));
		//	} catch(Exception x) {
		//		throw WrappedException.wrap(x); // Oh, the glory of checked exceptions. So useful. Sigh.
		//	}
		//} else {
			internalRenderTitle();
		//}
	}

	private void internalRenderTitle() {
		TD titleCell = getTitlePart();
		titleCell.removeAllChildren();
		if(isShowAsModified()) {
			titleCell.add("*" + getPageTitle());
		} else {
			titleCell.add(getPageTitle());
		}
		if(!DomUtil.isBlank(getHint())) {
			titleCell.setTitle(getHint());
		}
	}

	//public IRenderInto<String> getTitleNodeRenderer() {
	//	return m_titleNodeRenderer;
	//}

	// jal 20150929 Highly unwanted, pending removal
	///**
	// * Provide setting custom title node renderer.
	// * Parameters description for use for {@link INodeContentRenderer#renderNodeContent}:<BR/>
	// * <UL>
	// * <LI>NodeBase component is page title component.</LI>
	// * <LI>NodeContainer node is TD caption cell.</LI>
	// * <LI>String object is page caption.</LI>
	// * <LI>Object parameters is Boolean: T for modified flag, F for not modifed.</LI>
	// * </UL>
	// *
	// * @param titleNodeRenderer
	// */
	//public void setTitleNodeRenderer(INodeContentRenderer<String> titleNodeRenderer) {
	//	m_titleNodeRenderer = titleNodeRenderer;
	//}
}
