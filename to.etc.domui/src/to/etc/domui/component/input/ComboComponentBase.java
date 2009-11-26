package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class ComboComponentBase<T, V> extends SelectBasedControl<V> {
	private List<T> m_data;

	/** The specified ComboRenderer used. */
	private INodeContentRenderer<T> m_contentRenderer;

	private INodeContentRenderer<T> m_actualContentRenderer;

	private Class< ? extends INodeContentRenderer<T>> m_contentRendererClass;

	private PropertyMetaModel m_propertyMetaModel;

	/** When set this maker will be used to provide a list of values for this combo. */
	private IListMaker<T> m_listMaker;

	private Class< ? extends IComboDataSet<T>> m_dataSetClass;

	private IComboDataSet<T> m_dataSet;

	private IValueTransformer<V> m_valueTransformer;

	public ComboComponentBase() {}

	public ComboComponentBase(IListMaker<T> maker) {
		m_listMaker = maker;
	}

	public ComboComponentBase(IComboDataSet<T> dataSet) {
		m_dataSet = dataSet;
	}

	public ComboComponentBase(Class< ? extends IComboDataSet<T>> dataSetClass) {
		m_dataSetClass = dataSetClass;
	}

	public ComboComponentBase(List<T> in) {
		m_data = in;
	}

	public ComboComponentBase(Class< ? extends IComboDataSet<T>> set, INodeContentRenderer<T> r) {
		m_dataSetClass = set;
		m_contentRenderer = r;
	}

	@Override
	public void createContent() throws Exception {
		//-- Append shtuff to the combo
		int ix = 0;
		V raw = internalGetCurrentValue();
		if(!isMandatory()) {
			//-- Add 1st "empty" thingy representing the unchosen.
			SelectOption o = new SelectOption();
			if(getEmptyText() != null)
				o.setText(getEmptyText());
			add(o);
			if(raw == null) {
				o.setSelected(true);
				internalSetSelectedIndex(0);
			}
			ix++;
		}

		ClassMetaModel cmm = null;
		for(T val : getData()) {
			SelectOption o = new SelectOption();
			add(o);
			renderOptionLabel(o, val);
			V res = listToValue(val);
			if(cmm == null)
				cmm = MetaManager.findClassMeta(res.getClass());
			boolean eq = MetaManager.areObjectsEqual(res, raw, cmm);
			if(eq) {
				o.setSelected(eq);
				internalSetSelectedIndex(ix);
			}
			ix++;
		}
	}

	/**
	 * Find the index of the value [newvalue].
	 * @see to.etc.domui.component.input.SelectBasedControl#findListIndexForValue(java.lang.Object)
	 */
	@Override
	protected int findListIndexForValue(V newvalue) {
		try {
			ClassMetaModel	cmm = newvalue == null ? null : MetaManager.findClassMeta(newvalue.getClass());;
			List<T> data = getData();
			for(int ix = 0; ix < data.size(); ix++) {
				V	value = listToValue(data.get(ix));
				if(MetaManager.areObjectsEqual(value, newvalue, cmm))
					return ix;
			}
			return -1;
		} catch(Exception x) { // Need to wrap; James Gosling is an idiot.
			throw WrappedException.wrap(x);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.component.input.SelectBasedControl#findOptionValueByIndex(int)
	 */
	@Override
	protected V findOptionValueByIndex(int ix) {
		try {
			List<T> data = getData();
			if(ix < 0 || ix >= data.size())
				return null;
			return listToValue(data.get(ix));
		} catch(Exception x) { // Need to wrap; James Gosling is an idiot.
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

	private INodeContentRenderer<T> calculateContentRenderer(Object val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(m_contentRendererClass != null)
			return DomApplication.get().createInstance(m_contentRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (INodeContentRenderer<T>) MetaManager.createDefaultComboRenderer(m_propertyMetaModel, cmm);
	}

	protected void renderOptionLabel(SelectOption o, T object) throws Exception {
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = calculateContentRenderer(object);
		m_actualContentRenderer.renderNodeContent(this, o, object, this);
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
	public List<T> getData() throws Exception {
		if(m_data == null)
			m_data = provideData();
		return m_data;
	}

	/**
	 * Creates the list-of-values that is to be used if no specific lov is set using setData(). The
	 * default implementation walks the data providers to see if one is present.
	 * @return
	 * @throws Exception
	 */
	protected List<T> provideData() throws Exception {
		if(m_listMaker != null)
			return DomApplication.get().getCachedList(m_listMaker);

		//-- Try datasets,
		IComboDataSet<T> builder = m_dataSet;
		if(builder == null && m_dataSetClass != null)
			builder = DomApplication.get().createInstance(m_dataSetClass);
		if(builder != null)
			return builder.getComboDataSet(getPage().getConversation(), null);
		throw new IllegalStateException("I have no way to get data to show in my combo..");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getters, setters and other boring crud.				*/
	/*--------------------------------------------------------------*/
	public INodeContentRenderer<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	public Class< ? extends INodeContentRenderer<T>> getContentRendererClass() {
		return m_contentRendererClass;
	}

	public void setContentRendererClass(Class< ? extends INodeContentRenderer<T>> contentRendererClass) {
		m_contentRendererClass = contentRendererClass;
	}

	public PropertyMetaModel getPropertyMetaModel() {
		return m_propertyMetaModel;
	}

	public void setPropertyMetaModel(PropertyMetaModel propertyMetaModel) {
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
}
