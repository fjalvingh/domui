package to.etc.domui.component.layout;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Small header component to separate items vertically on a page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 14, 2010
 */
public class CaptionedHeader extends Table {
	private String m_caption;

	private TD m_buttonpart;

	List<SmallImgButton> m_btns = Collections.EMPTY_LIST;

	public CaptionedHeader() {}

	public CaptionedHeader(String ttl) {
		m_caption = ttl;
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
		setCssClass("ui-chdr");
		setCellPadding("0");
		setCellSpacing("0");
		setTableWidth("100%");
		TBody b = addBody();
		TD left = b.addRowAndCell();
		left.setCssClass("ui-chdr-l");

		TD ttltd = b.addCell();
		ttltd.setCssClass("ui-chdr-c");
		ttltd.setNowrap(true);
		Div ttl = new Div();
		ttltd.add(ttl);
		ttl.setCssClass("ui-chdr-ttl");
		ttl.setText(m_caption);
		TD right = b.addCell();
		right.setCssClass("ui-chdr-r");
		m_buttonpart = right;
		right.setAlign(TDAlignType.RIGHT);
		for(SmallImgButton btn : m_btns) {
			m_buttonpart.add(btn);
		}
	}

	public void addButton(String image, String hint, IClicked<NodeBase> handler) {
		if(m_btns == Collections.EMPTY_LIST) {
			m_btns = new ArrayList<SmallImgButton>();
		}
		SmallImgButton ib = new SmallImgButton(image);
		ib.setTitle(hint);
		ib.setClicked(handler);
		m_btns.add(ib);
	}

	public void addButton(String image, String hint, String onClickJs) {
		if(m_btns == Collections.EMPTY_LIST) {
			m_btns = new ArrayList<SmallImgButton>();
		}
		SmallImgButton ib = new SmallImgButton(image);
		ib.setTitle(hint);
		ib.setOnClickJS(onClickJs);
		m_btns.add(ib);
	}
}
