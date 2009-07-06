package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

public class ComboComponentBase<T, V> extends SpanBasedControl<V> {
	/** The name of the set of ReferenceCodes to show in this combo */
	private Select m_combo;

	private List<SmallImgButton> m_buttonList = Collections.EMPTY_LIST;

	private List<T> m_data;

	/** The text value to show on the "unselected" option */
	private String m_emptyText;

	/** The specified ComboRenderer used. */
	private INodeContentRenderer<T> m_contentRenderer;

	private INodeContentRenderer<T> m_actualContentRenderer;

	private Class< ? extends INodeContentRenderer<T>> m_contentRendererClass;

	private PropertyMetaModel m_propertyMetaModel;

	/** When set this maker will be used to provide a list of values for this combo. */
	private IListMaker<T> m_listMaker;

	private IValueTransformer<V> m_valueTransformer;

	public ComboComponentBase() {}

	public ComboComponentBase(IListMaker<T> maker) {
		m_listMaker = maker;
	}

	public ComboComponentBase(List<T> in) {
		m_data = in;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		m_combo = new Select() {
			@Override
			public void acceptRequestParameter(String[] values) throws Exception {
				String in = values[0]; // Must be the ID of the selected Option thingy.
				SelectOption selo = (SelectOption) getPage().findNodeByID(in);
				if(selo == null) {
					setRawValue(null);
				} else {
					int index = findChildIndex(selo); // Must be found
					if(index == -1)
						throw new IllegalStateException("Where has my child " + in + " gone to??");
					this.setSelectedIndex(index);
					if(!ComboComponentBase.this.isMandatory()) {
						//-- If the index is 0 we have the "unselected" thingy; if not we need to decrement by 1 to skip that entry.
						if(index == 0)
							setRawValue(null);
						index--; // IMPORTANT Index becomes -ve if value lookup may not be done!
					}

					if(index >= 0) {
						List<T> data = getData();
						if(index >= data.size()) {
							setRawValue(null);
						} else
							setRawValue(listToValue(data.get(index)));
					}
				}
			}
		};
		add(m_combo);
		for(SmallImgButton b : m_buttonList)
			add(b);

		//-- Append shtuff to the combo
		if(!isMandatory()) {
			//-- Add 1st "empty" thingy representing the unchosen.
			SelectOption o = new SelectOption();
			if(getEmptyText() != null)
				o.setText(getEmptyText());
			m_combo.add(o);
			o.setSelected(getRawValue() == null);
		}

		ClassMetaModel cmm = null;
		for(T val : getData()) {
			SelectOption o = new SelectOption();
			m_combo.add(o);
			renderOptionLabel(o, val);
			V res = listToValue(val);
			if(cmm == null)
				cmm = MetaManager.findClassMeta(res.getClass());
			boolean eq = MetaManager.areObjectsEqual(res, getRawValue(), cmm);
			o.setSelected(eq);
		}
	}

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

	public void addExtraButton(String img, String title, final IClicked<ComboComponentBase<T, V>> clicked) {
		if(m_buttonList == Collections.EMPTY_LIST)
			m_buttonList = new ArrayList<SmallImgButton>();
		SmallImgButton si = new SmallImgButton(img);
		if(clicked != null) {
			si.setClicked(new IClicked<SmallImgButton>() {
				public void clicked(SmallImgButton b) throws Exception {
					clicked.clicked(ComboComponentBase.this);
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
		throw new IllegalStateException("I have no way to get data to show in my combo..");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getters, setters and other boring crud.				*/
	/*--------------------------------------------------------------*/
	public String getEmptyText() {
		return m_emptyText;
	}

	public void setEmptyText(String emptyText) {
		m_emptyText = emptyText;
	}

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
