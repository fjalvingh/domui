package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

abstract public class SelectBasedControl<T> extends Select implements IInputNode<T>, IHasModifiedIndication {
	private String m_emptyText;

	private T m_currentValue;

	private List<SmallImgButton> m_buttonList = Collections.EMPTY_LIST;

	/**
	 * Locate the "T" value for the nth selected option. This must return the ACTUAL list value and must
	 * not decrement the index for mandatoryness (this has already been done).
	 * @param nindex
	 * @return
	 */
	abstract protected T findListValueByIndex(int nindex);

	abstract protected int findOptionIndexForValue(T newvalue);

	public String getEmptyText() {
		return m_emptyText;
	}

	public void setEmptyText(String emptyText) {
		m_emptyText = emptyText;
	}

	/**
	 * The user selected a different option.
	 * @see to.etc.domui.dom.html.Select#internalOnUserInput(int, int)
	 */
	@Override
	protected boolean internalOnUserInput(int oldindex, int nindex) {
		T	newval;
		if(nindex <= 0)
			newval = null;
		else {
			nindex--;
			newval = findListValueByIndex(nindex);
		}
		ClassMetaModel cmm = newval == null ? null : MetaManager.findClassMeta(newval.getClass());
		if(MetaManager.areObjectsEqual(newval, m_currentValue, cmm))
			return false;
		m_currentValue = newval;
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Code to add extra stuff after this combo.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a small image button after the combo.
	 * @param img
	 * @param title
	 * @param clicked
	 */
	public void addExtraButton(String img, String title, final IClicked<SelectBasedControl<T>> click) {
		if(m_buttonList == Collections.EMPTY_LIST)
			m_buttonList = new ArrayList<SmallImgButton>();
		SmallImgButton si = new SmallImgButton(img);
		if(click != null) {
			si.setClicked(new IClicked<SmallImgButton>() {
				public void clicked(SmallImgButton b) throws Exception {
					click.clicked(SelectBasedControl.this);
				}
			});
		}
		if(title != null)
			si.setTitle(title);
		si.addCssClass("ui-cl2-btn");
		m_buttonList.add(si);

		if(isBuilt())
			forceRebuild();
	}

	@Override
	public void onAddedToPage(Page p) {
		NodeContainer curr = this;
		for(SmallImgButton sib : m_buttonList) {
			curr.appendAfterMe(sib);
			curr = sib;
		}
	}

	@Override
	public void onRemoveFromPage(Page p) {
		for(SmallImgButton sib : m_buttonList) {
			sib.remove();
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IInputNode<T> implementation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	final public T getValue() {
		if(isMandatory() && m_currentValue == null) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.NOT_VALID, "null");
		}
		return m_currentValue;
	}

	/**
	 *
	 * @see to.etc.domui.dom.html.IInputNode#setValue(java.lang.Object)
	 */
	final public void setValue(T v) {
		ClassMetaModel cmm = v != null ? MetaManager.findClassMeta(v.getClass()) : null;
		if(MetaManager.areObjectsEqual(v, m_currentValue, cmm))
			return;
		T prev = m_currentValue;
		m_currentValue = v;
		if(!isBuilt())
			return;
		internalOnValueSet(prev, m_currentValue);
	}

	/**
	 * A value was set through setValue(); we need to find the proper thingy to select.
	 */
	final protected void internalOnValueSet(T previousvalue, T newvalue) {
		int ix = findOptionIndexForValue(newvalue);
		setSelectedIndex(ix);
	}

	protected T internalGetCurrentValue() {
		return m_currentValue;
	}

	protected void internalSetCurrentValue(T val) {
		m_currentValue = val;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/
	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}
}
