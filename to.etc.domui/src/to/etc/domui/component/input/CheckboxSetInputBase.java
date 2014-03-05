package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

abstract public class CheckboxSetInputBase<V, T> extends AbstractDivControl<Set<V>> {
	private List<T> m_data;

	/** The specified ComboRenderer used. */
	private INodeContentRenderer<T> m_contentRenderer;

	private INodeContentRenderer<T> m_actualContentRenderer;

	private Map<V, Checkbox> m_checkMap = new HashMap<>();

	@Nonnull
	abstract protected V listToValue(@Nonnull T in) throws Exception;

	public CheckboxSetInputBase() {}

	public CheckboxSetInputBase(@Nonnull List<T> data) {
		m_data = data;
	}

	/**
	 * Can be used to set a specific list-of-values. When called this clears the existing dataset.
	 * @param data
	 */
	public void setData(@Nullable List<T> data) {
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
	@Nullable
	public List<T> getData() throws Exception {
		return m_data;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-cbis");
		m_checkMap.clear();
		List<T> data = getData();
		int count = 0;
		if(null != data) {
			for(T lv : data) {
				if(count++ != 0) {
					add(" ");
				}
				renderCheckbox(lv);
			}
		}
	}

	private void renderCheckbox(@Nonnull T lv) throws Exception {
		V listval = listToValue(lv);

		Checkbox cb = new Checkbox();
		Collection<V> value = getValue();
		if(value != null && value.contains(lv))
			cb.setChecked(true);
		boolean disa = isDisabled() || isReadOnly();
		cb.setReadOnly(disa);

		add(cb);
		INodeContentRenderer<T> cr = m_actualContentRenderer;
		if(cr == null)
			cr = m_actualContentRenderer = calculateContentRenderer(lv);
		cr.renderNodeContent(this, this, lv, cb);
		m_checkMap.put(listval, cb);

		final IValueChanged<CheckboxSetInputBase<V, T>> ovc = (IValueChanged<CheckboxSetInputBase<V, T>>) getOnValueChanged();
		if(ovc != null) {
			cb.setClicked(new IClicked<Checkbox>() {
				@Override
				public void clicked(@Nonnull Checkbox clickednode) throws Exception {
					ovc.onValueChanged(CheckboxSetInputBase.this);
				}
			});
		}
	}

	private INodeContentRenderer<T> calculateContentRenderer(T val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (INodeContentRenderer<T>) MetaManager.createDefaultComboRenderer(null, cmm);
	}

	protected void renderOptionLabel(SelectOption o, T object) throws Exception {
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = calculateContentRenderer(object);
		m_actualContentRenderer.renderNodeContent(this, o, object, this);
	}

	@Override
	public Set<V> getValue() {
		Set<V> value = super.getValue();
		if(null == value)
			value = Collections.EMPTY_SET;
		updateValue(value);
		return value;
	}

	private void updateValue(@Nonnull Set<V> value) {
		for(Map.Entry<V, Checkbox> me : m_checkMap.entrySet()) {
			if(me.getValue().isChecked()) {
				value.add(me.getKey());
			} else {
				value.remove(me.getKey());
			}
		}
	}

	@Override
	public void setValue(@Nullable Set<V> v) {
		getValue();									// Update set to the latest checkbox states.
		super.setValue(v);
	}

	@Override
	protected void disabledChanged() {
		boolean disa = isDisabled() || isReadOnly();
		for(Checkbox cb : m_checkMap.values()) {
			cb.setReadOnly(disa);
		}
	}

	@Override
	protected void readOnlyChanged() {
		disabledChanged();
	}

	@Override
	public void moveControlToModel() throws Exception {
		getValue();
	}

}
