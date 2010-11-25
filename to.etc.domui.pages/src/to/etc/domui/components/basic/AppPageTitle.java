package to.etc.domui.components.basic;

import to.etc.domui.annotations.*;
import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A page title bar. This consists of an image in the left corner, a string describing the
 * module's functionality and a set of standard buttons opening quick-access pages. This uses
 * the generic menu system code to retrieve a module name and image, if applicable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class AppPageTitle extends Div {
	private final Img m_img = new Img();

	private String m_title;

	private TD m_buttonpart = new TD();

	private TD m_titlePart;

	private String m_imageUrl;

	private String m_hint;

	private boolean m_showAsModified;

	private INodeContentRenderer<String> m_titleNodeRenderer;

	public AppPageTitle() {}

	public AppPageTitle(final String title) {
		m_title = title;
	}

	protected AppPageTitle(final String icon, final String title) {
		m_title = title;
		setIcon(icon);
	}

	public void setIcon(final String s) {
		m_imageUrl = s;
	}

	public String getHint() {
		return m_hint;
	}

	public void setHint(String hint) {
		m_hint = hint;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		Table tbl = new Table();
		add(tbl);
		TBody b = new TBody();
		tbl.add(b);
		tbl.setCssClass("vp-ttl");
		tbl.setCellPadding("0");
		tbl.setCellSpacing("0");
		tbl.setTableBorder(0);
		TR tr = b.addRow();
		b.add(tr);

		//-- Image...
		setIconURL();
		//		if(m_img.getSrc() == null)
		//			m_img.setSrc("img/btnModule.png");
		m_img.setAlign(ImgAlign.LEFT);
		TD td = b.addCell();
		td.add(m_img);
		td.setCssClass("vp-ttl-i");

		//-- Title.
		td = b.addCell();
		m_titlePart = td;
		td.setCssClass("vp-ttl-t");
		td.setTestID("pageTitle");
		renderTitleCell();

		//-- Buttons
		b.row().add(m_buttonpart);
		//		td = b.addCell();
		m_buttonpart.setCssClass("vp-ttl-bb");
		//		td.setWidth("1%");
		addDefaultButtons(m_buttonpart);
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

	/**
	 * Return the title that is used by this bar. If no user title is set this returns the
	 * calculated title (from annotations and metadata).
	 * @return
	 */
	public String getPageTitle() {
		if(m_title != null) // Manually set?
			return m_title;
		return DomUtil.calcPageTitle(getPage().getBody().getClass());
	}

	public void setPageTitle(String ttl) throws Exception {
		if(DomUtil.isEqual(m_title, ttl))
			return;

		m_title = ttl;
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
		SmallImgButton ib = new SmallImgButton("img/btnSpecialChar.png");
		nc.add(ib);
		ib.setTitle("Toon lijst van bijzondere tekens");
		ib.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase b) throws Exception {
				OddCharacters oc = new OddCharacters();
				getPage().getBody().add(oc);
			}
		});
	}

	public TD getTitlePart() {
		return m_titlePart;
	}

	public boolean isShowAsModified() {
		return m_showAsModified;
	}

	public void setShowAsModified(boolean showAsModified) throws Exception {
		if(m_showAsModified != showAsModified) {
			m_showAsModified = showAsModified;
			if(isBuilt()) {
				renderTitleCell();
			}
		}
	}

	private void renderTitleCell() throws Exception {
		TD titleCell = getTitlePart();
		if(m_titleNodeRenderer != null) {
			m_titleNodeRenderer.renderNodeContent(this, titleCell, getPageTitle(), Boolean.valueOf(m_showAsModified));
		} else {
			internalRenderTitle();
		}
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

	public INodeContentRenderer<String> getTitleNodeRenderer() {
		return m_titleNodeRenderer;
	}

	/**
	 * Provide setting custom title node renderer.
	 * Parameters description for use for {@link INodeContentRenderer#renderNodeContent}:<BR/>
	 * <UL>
	 * <LI>NodeBase component is page title component.</LI>
	 * <LI>NodeContainer node is TD caption cell.</LI>
	 * <LI>String object is page caption.</LI>
	 * <LI>Object parameters is Boolean: T for modified flag, F for not modifed.</LI>
	 * </UL>
	 *
	 * @param titleNodeRenderer
	 */
	public void setTitleNodeRenderer(INodeContentRenderer<String> titleNodeRenderer) {
		m_titleNodeRenderer = titleNodeRenderer;
	}
}
