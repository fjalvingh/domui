package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.ImgAlign;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TDAlignType;
import to.etc.domui.dom.html.Table;
import to.etc.domui.util.DomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A multi-format caption component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2013
 */
public class Caption2 extends Div {
	private String m_caption;

	private TD m_right;

	private List<SmallImgButton> m_btns = Collections.EMPTY_LIST;

	private Table m_table;

	private TD m_left;

	private TD m_middle;

	private Img m_icon;

	private Div m_ttldiv;

	@NonNull
	private String m_rootCss = CaptionType.Default.getCssClass();

	public Caption2(@NonNull CaptionType type) {
		m_rootCss = type.getCssClass();
	}

	public Caption2(@NonNull CaptionType type, @NonNull String caption) {
		m_rootCss = type.getCssClass();
		m_caption = caption;
	}

	public Caption2(@NonNull String cssClass) {
		m_rootCss = cssClass;
	}

	public Caption2(@NonNull String cssClass, @NonNull String title) {
		m_rootCss = cssClass;
		m_caption = title;
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
		addCssClass(m_rootCss + " ui-cptn2");
		Table tbl = m_table = new Table();
		add(tbl);
		tbl.setCellPadding("0");
		tbl.setCellSpacing("0");
		tbl.setTableWidth("100%");
		TBody b = new TBody();
		tbl.add(b);
		TD td = m_left = b.addRowAndCell();
		td.setCssClass(m_rootCss + " ui-cptn2-left");
		td = m_middle = b.addCell();

		td.setCssClass("ui-cptn2-middle");
		m_middle.setNowrap(true);
		Div d = m_ttldiv = new Div();
		m_middle.add(d);

		//		ttl.setCssClass("ui-cptn-ttl");
		d.add(m_caption);
		TD right = m_right = b.addCell();
		right.setCssClass("ui-cptn2-right");
		right.setAlign(TDAlignType.RIGHT);

		if(m_icon != null)
			m_left.add(m_icon);

		for(SmallImgButton btn : m_btns) {
			m_right.add(btn);
		}
	}

	public void setIcon(@NonNull String src) {
		Img img = m_icon;
		if(img == null) {
			img = m_icon = new Img(src);
			if(isBuilt()) {
				m_left.removeAllChildren();
				m_left.add(0, img);
			}
		} else
			img.setSrc(src);
		img.setAlign(ImgAlign.RIGHT);
	}

	public void addButton(@NonNull IIconRef image, @Nullable String hint, @NonNull IClicked<NodeBase> handler) {
		SmallImgButton ib = new SmallImgButton(image);
		ib.setClicked(handler);
		internallyAddButton(ib, hint);
	}

	public void addButton(@NonNull IIconRef image, @Nullable String hint, @NonNull String onClickJs) {
		SmallImgButton ib = new SmallImgButton(image);
		ib.setOnClickJS(onClickJs);
		internallyAddButton(ib, hint);
	}

	private void internallyAddButton(@NonNull SmallImgButton ib, @Nullable String hint) {
		if(m_btns == Collections.EMPTY_LIST) {
			m_btns = new ArrayList<SmallImgButton>();
		}
		ib.setTitle(hint);
		m_btns.add(ib);
		if(isBuilt() && m_right != null) {
			m_right.add(ib);
		}
	}
}
