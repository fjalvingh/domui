package to.etc.domui.component.headers;

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/21/15.
 */
@DefaultNonNull
public class GenericHeader extends Div {
	@Nullable
	private String m_text;

	public enum Type {
		/** A simple header with just a bigger font, in black */
		SIMPLE

		/** A simple header with a bigger font, in blue */
		, BLUE

		/* Generic styles */
		, HEADER_1
		, HEADER_2
		, HEADER_3
		, HEADER_4
	}

	final private Type m_type;

	private List<SmallImgButton> m_btns = Collections.EMPTY_LIST;

	@Nullable
	private Div m_buttonPart;

	public GenericHeader(Type type, String text) {
		m_type = type;
		m_text = text;
	}

	public GenericHeader(String text) {
		this(Type.SIMPLE, text);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-generichd ui-generichd-" + m_type.name().toLowerCase().replace('_', '-'));
		add(m_text);
		m_buttonPart = null;
		renderButtons();
	}

	private void renderButtons() {
		if(m_btns.size() == 0)
			return;
		Div part = m_buttonPart = new Div("ui-generichd-btns") ;
		add(part);
		for(SmallImgButton btn : m_btns) {
			part.add(btn);
		}
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
			m_btns = new ArrayList<>();
		}
		ib.setTitle(hint);
		m_btns.add(ib);
		if(isBuilt() && m_buttonPart != null) {
			m_buttonPart.add(ib);
		}
	}
}
