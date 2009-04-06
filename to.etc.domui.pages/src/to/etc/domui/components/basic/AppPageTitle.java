package to.etc.domui.components.basic;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.utils.*;

/**
 * A page title bar. This consists of an image in the left corner, a string describing the
 * module's functionality and a set of standard buttons opening quick-access pages. This uses
 * the generic menu system code to retrieve a module name and image, if applicable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class AppPageTitle extends Div {
	private final Img		m_img = new Img();
	private String			m_title;
	private TD				m_buttonpart;
	private TD				m_titlePart;

	public AppPageTitle() {
	}
	protected AppPageTitle(final String title) {
		m_title	= title;
	}
	protected AppPageTitle(final String icon, final String title) {
		m_title	= title;
		setIcon(icon);
	}
	public void	setIcon(final String s) {
		m_img.setSrc(s);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		Table	tbl	= new Table();
		add(tbl);
		TBody	b	= new TBody();
		tbl.add(b);
		tbl.setCssClass("vp-ttl");
		tbl.setCellPadding("0");
		tbl.setCellSpacing("0");
		tbl.setTableBorder(0);
		TR	tr	= b.addRow();
		b.add(tr);

		//-- Image...
		if(m_img.getSrc() == null)
			m_img.setSrc("img/btnModule.png");
		m_img.setAlign(ImgAlign.LEFT);
		TD	td	= b.addCell();
		td.add(m_img);
		td.setCssClass("vp-ttl-i");

		//-- Title.
		td	= b.addCell();
		m_titlePart = td;
		td.setCssClass("vp-ttl-t");
		String ttl = getPageTitle();
		if(ttl != null)
			td.add(ttl);

		//-- Buttons
		td	= b.addCell();
		td.setCssClass("vp-ttl-bb");
//		td.setWidth("1%");
		m_buttonpart = td;
		addDefaultButtons(td);
	}
	public TD getButtonpart() {
		return m_buttonpart;
	}

	/**
	 * Calculate the image URL to use for the icon.
	 * @return
	 */
	private String	getIconURL() {
		return null;
	}

	/**
	 * Calculate the title URL to use for this thing.
	 * @return
	 */
	private String	getPageTitle() {
		if(m_title != null)						// Manually set?
			return m_title;
		return AppUIUtil.calcPageTitle(getPage().getBody().getClass());
	}

	protected void	addDefaultButtons(final NodeContainer nc) {
		SmallImgButton ib	= new SmallImgButton("img/btnSpecialChar.png");
		nc.add(ib);
		ib.setTitle("Toon lijst van bijzondere tekens");
		ib.setClicked(new IClicked<NodeBase>() {
			public void clicked(final NodeBase b) throws Exception {
				OddCharacters	oc = new OddCharacters();
				getPage().getBody().add(oc);
			}
		});
	}
}
