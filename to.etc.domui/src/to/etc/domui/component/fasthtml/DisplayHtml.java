package to.etc.domui.component.fasthtml;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Mini component to display an HTML section.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 16, 2010
 */
public class DisplayHtml extends Div implements IDisplayControl<String>, IBindable {
	private XmlTextNode m_xtn = new XmlTextNode();

	private boolean m_unchecked;

	public enum Mode {
		BLOCK, INLINE, INLINEBLOCK
	}

	private Mode m_mode = Mode.BLOCK;

	@Override
	public void createContent() throws Exception {
		switch(m_mode){
			default:
			case BLOCK:
				setCssClass("ui-dhtml-blk");
				break;
			case INLINE:
				setCssClass("ui-dhtml-inl");
				break;
			case INLINEBLOCK:
				setCssClass("ui-dhtml-ibl");
				break;
		}

		add(m_xtn);
	}

	@Override
	public String getValue() {
		return m_xtn.getText();
	}

	@Override
	public void setValue(String v) {
		if(!m_unchecked)
			v = DomUtil.htmlRemoveUnsafe(v);
		m_xtn.setText(v);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/
	/** When this is bound this contains the binder instance handling the binding. */
	@Nullable
	private DisplayOnlyBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Nonnull
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new DisplayOnlyBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}

	public boolean isUnchecked() {
		return m_unchecked;
	}

	public void setUnchecked(boolean unchecked) {
		m_unchecked = unchecked;
	}

	public Mode getMode() {
		return m_mode;
	}

	public void setMode(Mode mode) {
		if(mode == m_mode)
			return;
		m_mode = mode;
		forceRebuild();
	}
}
