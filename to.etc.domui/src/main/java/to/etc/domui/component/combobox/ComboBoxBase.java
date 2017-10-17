package to.etc.domui.component.combobox;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

import javax.annotation.*;
import java.util.*;

public class ComboBoxBase<T, V> extends Div implements IControl<V> {
	private boolean m_mandatory;

	private boolean m_disabled;

	private boolean m_readOnly;

	private String m_emptyText;

	private V m_currentValue;

	private int m_currentIndex;

	private IValueChanged< ? > m_onValueChanged;

	/**
	 * If this combobox has a "unselected" option currently this contains that option. When present it
	 * means that indexes in the <i>combo</i> list are one <i>higher</i> than indexes in the backing
	 * dataset (because this empty option is always choice# 0).
	 */
	private ComboOption<T> m_emptyOption;

	private List<T> m_data;

	/** The specified ComboRenderer used. */
	private IRenderInto<T> m_contentRenderer;

	private IRenderInto<T> m_actualContentRenderer;

	private Class< ? extends IRenderInto<T>> m_contentRendererClass;

	private PropertyMetaModel< ? > m_propertyMetaModel;

	/** When set this maker will be used to provide a list of values for this combo. */
	private IListMaker<T> m_listMaker;

	private IComboDataSet<T> m_dataSet;

	private IValueTransformer<V> m_valueTransformer;

	private Div m_popupDiv;

	private Span m_valueNode;

	private Div m_arrowBox;

	public ComboBoxBase() {}

	public ComboBoxBase(@Nonnull IListMaker<T> maker) {
		m_listMaker = maker;
	}

	public ComboBoxBase(@Nonnull IComboDataSet<T> dataSet) {
		m_dataSet = dataSet;
	}

	public ComboBoxBase(@Nonnull QCriteria<T> query) {
		m_dataSet = new CriteriaComboDataSet<T>(query);
	}

	public ComboBoxBase(@Nonnull List<T> in) {
		m_data = in;
	}

	/**
	 * Render the actual combobox. This renders the value domain as follows:
	 * <ul>
	 *	<li>If the combobox is <i>optional</i> then the value list will always start with an "unselected" option
	 *		which will be shown if the value is null.</li>
	 *	<li>If the combobox is mandatory <i>but</i> it's current value is not part of the value domain (i.e. it
	 *		is null, or the value cannot be found in the list of values) then it <i>also</i> renders an initial
	 *		"unselected" option value which will become selected.</li>
	 *	<li>For a mandatory combo with a valid value the "empty" choice will not be rendered.</li>
	 * </ul>
	 * Fixes bug# 790.
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-cbb");
		Span span = m_valueNode = new Span();
		add(span);
		Div d = m_popupDiv = new Div();
		d.setCssClass("ui-cbb-pu");
		add(d);

		//-- Append shtuff to the combo
		List<T> list = getData();
		V raw = internalGetCurrentValue();

		//-- First loop over all values to find out if current value is part of value domain.
		boolean isvalidselection = false;
		int ix = 0;
		ClassMetaModel cmm = null;
		for(T val : list) {
			V res = listToValue(val);
			ComboOption<T> o = new ComboOption<T>(val);
			d.add(o);
			renderOptionLabel(o);

			//-- Handle "currently selected"
			if(null != raw) {
				if(cmm == null)
					//if we are caching cmm, then at least it should always be for one of compared values,
					//otherwise we can get situation that we are sending cmm of type that does not have any relation to any of compared values
					cmm = MetaManager.findClassMeta(raw.getClass());
				boolean eq = MetaManager.areObjectsEqual(res, raw, cmm);
				if(eq) {
					o.setSelected(eq);
					internalSetSelectedIndex(ix);
					isvalidselection = true;

					//-- render content value
					renderOptionLabel(span, o);
				}
			}
			ix++;
		}

		//-- Decide if an "unselected" option needs to be present, and add it at index 0 if so.
		setEmptyOption(null);
		if(!isMandatory() || !isvalidselection) {
			//-- We need the "unselected" option.
			ComboOption<T> o = new ComboOption<T>(null);
			if(getEmptyText() != null)
				o.setText(getEmptyText());
			add(0, o);										// Insert as the very 1st item
			setEmptyOption(o); 								// Save this to mark it in-use.
			if(!isvalidselection) {
				o.setSelected(true);
				internalSetSelectedIndex(0);
			} else
				internalSetSelectedIndex(getSelectedIndex() + 1); // Increment selected index thingy

			//-- render content value
			renderOptionLabel(span, o);
		}
		Div a = m_arrowBox = new Div();
		add(a);
		a.setCssClass("ui-cbb-ab");
		appendCreateJS("new WebUI.comboBox('" + getActualID() + "');");

	}

	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	private void internalSetSelectedIndex(int ix) {
		m_currentIndex = ix;
	}

	public int getSelectedIndex() {
		return m_currentIndex;
	}

	/**
	 * Set the selected index - expensive because it has to walk all Option children and reset their
	 * selected attribute - O(n) runtime.
	 * @param ix
	 */
	public void setSelectedIndex(int ix) {
		m_currentIndex = ix;
		for(int i = getChildCount(); --i >= 0;) {
			getOption(i).setSelected(i == m_currentIndex);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	value setting logic.								*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	final public V getValue() {
		if(isMandatory() && m_currentValue == null) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.MANDATORY);
		} else
			clearMessage();
		return m_currentValue;
	}

	/**
	 * Set the combo to the specified value. The value <i>must</i> be in the
	 * domain specified by the data list and must be findable in that list; if
	 * not <b>the results are undefined</b>.
	 * If the value set is null and the combobox is a mandatory one the code will
	 * check if an "unselected" item is present to select. If not the unselected
	 * item will be added by this call(!).
	 * @see to.etc.domui.dom.html.IControl#setValue(java.lang.Object)
	 */
	@Override
	final public void setValue(@Nullable V v) {
		ClassMetaModel cmm = v != null ? MetaManager.findClassMeta(v.getClass()) : null;
		V currentValue = m_currentValue;
		if(MetaManager.areObjectsEqual(v, currentValue, cmm))
			return;
		m_currentValue = v;
		if(!isBuilt())
			return;

		//-- If the value is NULL we MUST have an unselected option: add it if needed and select that one.
		int ix = findListIndexForValue(v);
		if(null == v || ix < 0) { 								// Also create "unselected" if the value is not part of the domain.
			if(getEmptyOption() == null) {
				//-- No empty option yet!! Create one;
				ComboOption<T> o = new ComboOption<T>(null);
				if(getEmptyText() != null)
					o.setText(getEmptyText());
				add(0, o); 										// Insert as the very 1st item
				setEmptyOption(o); 								// Save this to mark it in-use.
			}
			setSelectedIndex(0);
			return;
		}

		//-- Value is not null. Find the index of the option in the dataset
		if(getEmptyOption() != null)
			ix++;
		setSelectedIndex(ix);
	}

	/**
	 * The user selected a different option.
	 * @see to.etc.domui.dom.html.Select#internalOnUserInput(int, int)
	 */
	protected boolean internalOnUserInput(int oldindex, int nindex) {
		V newval;

		if(nindex < 0) {
			newval = null; // Should never happen
		} else if(getEmptyOption() != null) {
			//-- We have an "unselected" choice @ index 0. Is that one selected?
			if(nindex <= 0) // Empty value chosen?
				newval = null;
			else {
				nindex--;
				newval = findListValueByIndex(nindex);
			}
		} else {
			newval = findListValueByIndex(nindex);
		}

		ClassMetaModel cmm = newval == null ? null : MetaManager.findClassMeta(newval.getClass());
		V currentValue = m_currentValue;
		if(MetaManager.areObjectsEqual(newval, currentValue, cmm))
			return false;

		m_currentValue = newval;
		return true;
	}

	/**
	 * Return the index in the data list for the specified value, or -1 if not found or if the value is null.
	 * @param newvalue
	 * @return
	 */
	private int findListIndexForValue(V newvalue) {
		if(null == newvalue)
			return -1;
		try {
			ClassMetaModel cmm = MetaManager.findClassMeta(newvalue.getClass());
			List<T> data = getData();
			for(int ix = 0; ix < data.size(); ix++) {
				V value = listToValue(data.get(ix));
				if(MetaManager.areObjectsEqual(value, newvalue, cmm))
					return ix;
			}
			return -1;
		} catch(Exception x) { // Need to wrap; checked exceptions are idiotic
			throw WrappedException.wrap(x);
		}
	}

	private V findListValueByIndex(int ix) {
		try {
			List<T> data = getData();
			if(ix < 0 || ix >= data.size())
				return null;
			return listToValue(data.get(ix));
		} catch(Exception x) { // Need to wrap; checked exceptions are idiotic
			throw WrappedException.wrap(x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	List - value conversions and list management		*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param in
	 * @return
	 * @throws Exception
	 */
	protected V listToValue(T in) throws Exception {
		if(m_valueTransformer == null)
			return (V) in;
		return m_valueTransformer.getValue(in);
	}

	@Nonnull
	private IRenderInto<T> calculateContentRenderer(Object val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(m_contentRendererClass != null)
			return DomApplication.get().createInstance(m_contentRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (IRenderInto<T>) MetaManager.createDefaultComboRenderer(m_propertyMetaModel, cmm);
	}

	protected void renderOptionLabel(@Nonnull ComboOption<T> o) throws Exception {
		T value = o.getValue();
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = calculateContentRenderer(value);
		m_actualContentRenderer.renderOpt(o, value);
	}

	protected void renderOptionLabel(@Nonnull NodeContainer into, @Nonnull ComboOption<T> o) throws Exception {
		T value = o.getValue();
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = calculateContentRenderer(value);
		m_actualContentRenderer.renderOpt(into, value);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	All the myriad ways of providing data.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Can be used to set a specific list-of-values. When called this clears the existing dataset.
	 * @param data
	 */
	public void setData(List<T> data) {
		if(m_data != data) {
			forceRebuild();
			m_actualContentRenderer = null;
		}
		m_data = data;
	}

	/**
	 * Returns the data to use as the list-of-values of this combo. This must contain actual selectable
	 * values only, it may not contain a "no selection made" value thingerydoo.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public List<T> getData() throws Exception {
		List<T> data = m_data;
		if(data == null)
			data = m_data = provideData();
		return data;
	}

	/**
	 * Creates the list-of-values that is to be used if no specific lov is set using setData(). The
	 * default implementation walks the data providers to see if one is present.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	protected List<T> provideData() throws Exception {
		if(m_listMaker != null)
			return DomApplication.get().getCachedList(m_listMaker);

		//-- Try datasets,
		IComboDataSet<T> builder = m_dataSet;
		if(builder != null)
			return builder.getComboDataSet(getPage().getBody());
		return Collections.EMPTY_LIST;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IControl<T> implementation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public V getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getters, setters and other boring crud.				*/
	/*--------------------------------------------------------------*/
	public IRenderInto<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(IRenderInto<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	public Class< ? extends IRenderInto<T>> getContentRendererClass() {
		return m_contentRendererClass;
	}

	public void setContentRendererClass(Class< ? extends IRenderInto<T>> contentRendererClass) {
		m_contentRendererClass = contentRendererClass;
	}

	public PropertyMetaModel< ? > getPropertyMetaModel() {
		return m_propertyMetaModel;
	}

	public void setPropertyMetaModel(PropertyMetaModel< ? > propertyMetaModel) {
		m_propertyMetaModel = propertyMetaModel;
	}

	public IListMaker<T> getListMaker() {
		return m_listMaker;
	}

	public void setListMaker(IListMaker<T> listMaker) {
		m_listMaker = listMaker;
	}

	public IValueTransformer<V> getValueTransformer() {
		return m_valueTransformer;
	}

	public void setValueTransformer(IValueTransformer<V> valueTransformer) {
		m_valueTransformer = valueTransformer;
	}

	public String getEmptyText() {
		return m_emptyText;
	}

	public void setEmptyText(String emptyText) {
		m_emptyText = emptyText;
	}

	/**
	 * If this combobox has a "unselected" option currently this contains that option. When present it
	 * means that indexes in the <i>combo</i> list are one <i>higher</i> than indexes in the backing
	 * dataset (because this empty option is always choice# 0).
	 * @return
	 */
	@Nullable
	protected ComboOption<T> getEmptyOption() {
		return m_emptyOption;
	}

	@Nonnull
	private ComboOption<T> getOption(int ix) {
		if(ix < 0 || ix >= m_popupDiv.getChildCount())
			throw new ArrayIndexOutOfBoundsException("The option index " + ix + " is invalid, the #options is " + getChildCount());
		return (ComboOption<T>) m_popupDiv.getChild(ix);
	}

	/**
	 * See getter.
	 * @param emptyOption
	 */
	protected void setEmptyOption(ComboOption<T> emptyOption) {
		m_emptyOption = emptyOption;
	}

	@Nullable
	protected V internalGetCurrentValue() {
		return m_currentValue;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		if(m_mandatory == mandatory)
			return;
		m_mandatory = mandatory;
		forceRebuild(); 								// The "empty option" might have changed
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		m_readOnly = readOnly;
	}

	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		IValueChanged< ? > vc = m_onValueChanged;
		return vc;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

}
