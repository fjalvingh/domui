package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IRenderInto;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract public class CheckboxSetInputBase<V, T> extends AbstractDivControl<Set<V>> {
	private List<T> m_data;

	/**
	 * The specified ComboRenderer used.
	 */
	private IRenderInto<T> m_contentRenderer;

	private IRenderInto<T> m_actualContentRenderer;

	private Map<V, Checkbox> m_checkMap = new HashMap<>();

	private boolean m_asButtons;

	@NonNull
	abstract protected V listToValue(@NonNull T in) throws Exception;

	public CheckboxSetInputBase() {
	}

	public CheckboxSetInputBase(@NonNull List<T> data) {
		m_data = data;
	}

	/**
	 * Can be used to set a specific list-of-values. When called this clears the existing dataset.
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
	 */
	@Nullable
	public List<T> getData() throws Exception {
		return m_data;
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-cbis");
		Div setContainer = new Div("ui-cbis-c");
		add(setContainer);
		m_checkMap.clear();
		List<T> data = getData();
		int count = 0;
		if(null != data) {
			for(T lv : data) {
				renderCheckbox(setContainer, lv);
			}
		}
	}

	@Nullable
	@Override
	public NodeBase getForTarget() {
		List<Checkbox> children = getChildren(Checkbox.class);
		return children.size() > 0 ? children.get(0) : null;
	}

	@Nullable
	@Override
	protected String getFocusID() {
		NodeBase id = getForTarget();
		return id == null ? null : id.getActualID();
	}

	private void renderCheckbox(Div setContainer, @NonNull T lv) throws Exception {
		Div pair = new Div("ui-cbis-p");
		setContainer.add(pair);
		V listval = listToValue(lv);

		Checkbox cb = new Checkbox();
		Collection<V> value = getValue();
		if(value != null && value.contains(lv))
			cb.setChecked(true);
		boolean disa = isDisabled() || isReadOnly();
		cb.setReadOnly(disa);

		pair.add(cb);
		IRenderInto<T> cr = m_actualContentRenderer;
		if(cr == null)
			cr = m_actualContentRenderer = calculateContentRenderer(lv);
		Label span = new Label();
		span.setForTarget(cb);
		pair.add(span);
		cr.render(span, lv);
		m_checkMap.put(listval, cb);

		final IValueChanged<CheckboxSetInputBase<V, T>> ovc = (IValueChanged<CheckboxSetInputBase<V, T>>) getOnValueChanged();
		if(ovc != null) {
			cb.setClicked(new IClicked<Checkbox>() {
				@Override
				public void clicked(@NonNull Checkbox clickednode) throws Exception {
					ovc.onValueChanged(CheckboxSetInputBase.this);
				}
			});
		}
	}

	private IRenderInto<T> calculateContentRenderer(T val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (IRenderInto<T>) MetaManager.createDefaultComboRenderer(null, cmm);
	}

	//protected void renderOptionLabel(@NonNull SelectOption o, @NonNull T object) throws Exception {
	//	if(m_actualContentRenderer == null)
	//		m_actualContentRenderer = calculateContentRenderer(object);
	//	m_actualContentRenderer.render(o, object);
	//}

	@Override
	public Set<V> internalGetValue() {
		Set<V> value = super.internalGetValue();
		if(null == value) {
			value = new HashSet<>();
			super.internalSetValue(value);
		}
		updateValue(value);
		return value;
	}

	private void updateValue(@NonNull Set<V> value) {
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
		getValue();                                    // Update set to the latest checkbox states.
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
	public void setHint(String hintText) {
		setTitle(hintText);
	}

	protected CheckboxSetInputBase<V, T> asButtons() {
		m_asButtons = true;
		return this;
	}

	public IRenderInto<T> getRenderer() {
		return m_contentRenderer;
	}

	public void setRenderer(IRenderInto<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}
}
