package to.etc.domui.component.misc;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Display-only checkbox which renders better than a disabled checkbox thingy.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2010
 */
public class DisplayCheckbox extends Img implements IDisplayControl<Boolean>, IBindable {
	private Boolean m_value;

	public DisplayCheckbox() {
		setCssClass("ui-dspcb");
		setSrc("THEME/dspcb-off.png");
	}

	/**
	 *
	 * @see to.etc.domui.dom.html.IDisplayControl#getValue()
	 */
	@Override
	public Boolean getValue() {
		return m_value;
	}

	@Override
	public void setValue(Boolean v) {
		if(DomUtil.isEqual(v, m_value))
			return;
		m_value = v;
		if(v == null)
			setSrc("THEME/dspcb-off.png");
		else if(v.booleanValue())
			setSrc("THEME/dspcb-on.png");
		else
			setSrc("THEME/dspcb-off.png");
	}

	public void setChecked(boolean on) {
		setValue(Boolean.valueOf(on));
	}

	public boolean isChecked() {
		return getValue() != null && getValue().booleanValue();
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
}
