package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.RadioButton;
import to.etc.domui.dom.html.RadioGroup;
import to.etc.domui.util.IRenderInto;
import to.etc.function.PredicateEx;
import to.etc.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static java.util.Objects.requireNonNull;

@NonNullByDefault
abstract public class EnumValueListInputBase<V, E extends Enum<E>> extends AbstractDivControl<List<Pair<V, E>>> {

	@Nullable
	private List<V> m_data;

	private final Class<E> m_type;

	@Nullable
	private final E m_defaultValue;

	@Nullable
	private PredicateEx<V> m_isReadOnlyControl;

	@Nullable
	private BiPredicate<V, E> m_isEnumMemberApplicable;

	/**
	 * The specified renderer for variable representation content.
	 */
	@Nullable
	private IRenderInto<V> m_contentRenderer = null;

	@Nullable
	private IRenderInto<V> m_actualContentRenderer = null;

	/**
	 * The specified rendrer for enum values.
	 */
	@Nullable
	private IRenderInto<ValueLabelPair<E>> m_enumRenderer = null;

	private Map<V, IControl<E>> m_controlMap = new HashMap<>();

	private boolean m_asButtons;

	public EnumValueListInputBase(Class<E> type, @Nullable E defaultValue) {
		m_type = type;
		m_defaultValue = defaultValue;
	}

	public EnumValueListInputBase(Class<E> type, @Nullable E defaultValue, List<V> data) {
		this(type, defaultValue);
		m_data = data;
	}

	/**
	 * Used to set a specific list-of-variables. When called this clears the existing dataset.
	 */
	public void setData(@Nullable List<V> data) {
		if(m_data != data) {
			forceRebuild();
			m_actualContentRenderer = null;
		}
		m_data = data;
	}

	/**
	 * Returns the data to use as the list-of-variables of this control.
	 */
	@Nullable
	public List<V> getData() throws Exception {
		return m_data;
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-evli");
		Div setContainer = new Div("ui-evli-c");
		add(setContainer);
		m_controlMap.clear();
		List<V> data = getData();
		if(null != data) {
			for(V lv : data) {
				renderControl(setContainer, lv);
			}
		}
	}

	@Nullable
	@Override
	public NodeBase getForTarget() {
		if(m_asButtons) {
			return getChildren(RadioButton.class).stream().findFirst().orElse(null);
		}
		return getChildren(ComboFixed2.class).stream().findFirst().orElse(null);
	}

	@Nullable
	@Override
	protected String getFocusID() {
		NodeBase id = getForTarget();
		return id == null ? null : id.getActualID();
	}

	private void renderControl(Div setContainer, V lv) throws Exception {
		Div pair = new Div("ui-evli-p");
		setContainer.add(pair);
		IControl<E> control = m_controlMap.computeIfAbsent(lv, k -> createControlForKey(k));
		IRenderInto<V> cr = m_actualContentRenderer;
		if(cr == null)
			cr = m_actualContentRenderer = calculateContentRenderer(lv);
		Label span = new Label();
		span.setForTarget((NodeBase) control);
		pair.add(span);
		cr.render(span, lv);
		pair.add((NodeBase) control);
		PredicateEx<V> isReadOnlyControl = getIsReadOnlyControl();
		if(null!= isReadOnlyControl){
			if(isReadOnlyControl.test(lv)){
				control.setReadOnly(true);
			}
		}
	}

	@NonNull
	private IControl<E> createControlForKey(V key) {
		IControl<E> control;
		IRenderInto<ValueLabelPair<E>> enumRenderer = getEnumRenderer();
		if(m_asButtons) {
			E[] enums = m_type.getEnumConstants();
			ArrayList<E> skippedEnums =  new ArrayList<>();
			for(E oneEnum : enums){
				BiPredicate<V, E> isEnumMemberApplicable = getIsEnumMemberApplicable();
				if(null != isEnumMemberApplicable && !isEnumMemberApplicable.test(key, oneEnum)){
					skippedEnums.add(oneEnum);
				}
			}
			E[] skippedEnumsArray = skippedEnums.toArray((E[]) Array.newInstance(m_type, skippedEnums.size()));
			RadioGroup<E> rb = RadioGroup.createFromEnum(m_type, enumRenderer, skippedEnumsArray).asButtons();
			control = rb;
		}else {
			ComboFixed2<E> cb = ComboFixed2.createEnumCombo(m_type);
			control = cb;
			if(null != enumRenderer) {
				cb.setRenderer(enumRenderer);
			}
		}
		control.setMandatory(null != m_defaultValue);
		List<Pair<V, E>> currentValue = super.internalGetValue();
		E value = null == currentValue
			? m_defaultValue
			: currentValue.stream().filter(it -> key.equals(it.get1())).map(it -> it.get2()).findFirst().orElse(m_defaultValue);
		control.setValue(value);
		boolean disa = isDisabled() || isReadOnly();
		control.setReadOnly(disa);

		final IValueChanged<EnumValueListInputBase<V, E>> ovc = (IValueChanged<EnumValueListInputBase<V, E>>) getOnValueChanged();
		if(ovc != null) {
			control.setOnValueChanged(ctrl -> ovc.onValueChanged(EnumValueListInputBase.this));
		}
		return control;
	}

	private IRenderInto<V> calculateContentRenderer(V val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null variable");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return (IRenderInto<V>) MetaManager.createDefaultComboRenderer(null, cmm);
	}

	@Nullable
	@Override
	public List<Pair<V, E>> internalGetValue() {
		List<Pair<V, E>> value = super.internalGetValue();
		if(null == value) {
			value = new ArrayList<>();
			super.internalSetValue(value);
		}
		updateValue(value);
		return value;
	}

	private void updateValue(List<Pair<V, E>> values) {
		for(V key : data()) {
			IControl<E> control = m_controlMap.computeIfAbsent(key, k -> createControlForKey(k));
			E value = control.getValue();
			Pair<V, E> aPair = values.stream().filter(it -> key.equals(it.get1())).findFirst().orElse(null);
			if(null == aPair) {
				values.add(new Pair<>(key, value));
			}else if(aPair.get2() != value) {
				values.remove(aPair);
				values.add(new Pair<>(key, value));
			}
		}
	}

	@Override
	protected void internalSetValue(@Nullable List<Pair<V, E>> values) {
		super.internalSetValue(values);
		for(V key : data()) {
			IControl<E> control = m_controlMap.computeIfAbsent(key, k -> createControlForKey(k));
			Pair<V, E> aPair = null == values
				? null
				: values.stream().filter(it -> key.equals(it.get1())).findFirst().orElse(null);
			if(null == aPair) {
				control.setValue(m_defaultValue);
			} else {
				control.setValue(aPair.get2());
			}
		}
	}

	@Override
	public void setValue(@Nullable List<Pair<V, E>> v) {
		super.setValue(v);
	}

	@Override
	protected void disabledChanged() {
		boolean disa = isDisabled() || isReadOnly();
		for(IControl<E> cb : m_controlMap.values()) {
			cb.setReadOnly(disa);
		}
	}

	@Override
	protected void readOnlyChanged() {
		disabledChanged();
	}

	@Override
	public void setHint(@Nullable String hintText) {
		setTitle(hintText);
	}

	protected EnumValueListInputBase<V, E> asButtons() {
		m_asButtons = true;
		return this;
	}

	@Nullable
	public IRenderInto<V> getRenderer() {
		return m_contentRenderer;
	}

	public void setRenderer(IRenderInto<V> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	@Nullable
	public IRenderInto<ValueLabelPair<E>> getEnumRenderer() {
		return m_enumRenderer;
	}

	public void setEnumRenderer(IRenderInto<ValueLabelPair<E>> enumRenderer) {
		m_enumRenderer = enumRenderer;
		if(isBuilt()) {
			forceRebuild();
		}
	}

	@Nullable
	public BiPredicate<V, E> getIsEnumMemberApplicable() {
		return m_isEnumMemberApplicable;
	}

	public void setIsEnumMemberApplicable(@Nullable BiPredicate<V, E> isEnumMemberApplicable) {
		m_isEnumMemberApplicable = isEnumMemberApplicable;
	}

	@Nullable
	public PredicateEx<V> getIsReadOnlyControl() {
		return m_isReadOnlyControl;
	}

	public void setIsReadOnlyControl(@Nullable PredicateEx<V> isReadOnlyControl) {
		m_isReadOnlyControl = isReadOnlyControl;
	}
	
	private List<V> data() {
		return requireNonNull(m_data, "data is not initialized!");
	}
}
