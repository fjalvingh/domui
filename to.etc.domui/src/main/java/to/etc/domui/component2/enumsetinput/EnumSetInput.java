package to.etc.domui.component2.enumsetinput;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.component.input.SearchAsYouType;
import to.etc.domui.component.input.SearchAsYouTypeBase;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.IRenderInto;
import to.etc.util.WrappedException;
import to.etc.webapp.nls.NlsContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * FIXME Functional duplicate of LabelSelector.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-2-18.
 */
@NonNullByDefault
public class EnumSetInput<T> extends AbstractDivControl<Set<T>> {
	private final Class<T> m_actualClass;

	private final String m_property;

	@NonNull
	private List<T> m_dataList = new ArrayList<>();

	private final Map<T, Div> m_displayMap = new HashMap<>();

	@Nullable
	private Function<T, String> m_converter;

	@Nullable
	private BiFunction<T, String, Boolean> m_predicate;

	@Nullable
	private IRenderInto<T> m_renderer;

	private boolean m_addSingleMatch = true;

	@Nullable
	private SearchAsYouType<T> m_input;

	public EnumSetInput(Class<T> actualClass, String property) {
		m_actualClass = actualClass;
		m_property = property;
	}

	public EnumSetInput(Class<T> actualClass, List<T> data, String property) {
		m_actualClass = actualClass;
		m_property = property;
		m_dataList = data;
	}

	@Override public void createContent() throws Exception {
		addCssClass("ui-esic");
		m_displayMap.clear();
		Set<T> set = getValue();
		if(null != set) {
			//-- Order the set by alphabetical text representation of the thingy
			List<T> list = new ArrayList<>(set);
			list.sort(this::compareText);

			for(T t : list) {
				Div label = renderLabel(t);
				add(label);
			}
		}
		SearchAsYouType<T> input = m_input = new SearchAsYouType<>(m_actualClass, m_property);
		//input.setCssBase("ui-esic-input");
		add(input);
		input.setAddSingleMatch(isAddSingleMatch());
		//input.setCssClass("ui-esic-input");
		input.setData(getData());

		input.setOnValueChanged(a -> {
			T value = input.getValue();
			if(null != value) {
				addItem(value);
				input.setValue(null);
				updateValueList();
			}
		});

		input.setDisabled(isDisabled());
		input.setReadOnly(isReadOnly());
	}

	private void updateValueList() {
		List<T> list = new ArrayList<>();
		List<T> source = m_dataList;

		Set<T> value = internalGetValue();
		if(null == value)
			value = Collections.emptySet();
		if(null != source) {
			for(T datum : source) {
				if(! value.contains(datum))
					list.add(datum);
			}
		}
		requireNonNull(m_input).setData(list);
	}

	private Div renderLabel(T value) throws Exception {
		Div label = new Div();
		label.css("ui-esic-label");
		IRenderInto<T> renderer = m_renderer;
		if(null == renderer) {
			String labelText = getLabelText(value);
			label.add(new Span("ui-esic-ltxt", labelText));
		} else {
			renderer.render(label, value);
		}

		//-- Add the "remove" button to the label
		if(!isDisabled() && !isReadOnly()) {
			Button delBtn = new Button().css("ui-esic-del");
			label.add(delBtn);
			delBtn.add(Icon.faTimes.createNode());
			delBtn.setClicked(a -> {
				removeItem(value);
				SearchAsYouType<T> input = m_input;
				if(input != null) {
					input.setFocus();
					updateValueList();
				}

			});
		}
		m_displayMap.put(value, label);					// Register
		return label;
	}

	private void removeItem(T value) throws Exception {
		//-- Remove this from screen and from the value.
		Set<T> set = getValue();
		if(null != set) {
			set.remove(value);
			valueHasChanged();
		}
		Div node = m_displayMap.remove(value);
		if(null != node) {
			node.remove();
		}
	}

	public void addItem(T item) throws Exception {
		Set<T> set = getValue();
		if(null == set) {
			set = new HashSet<>();
		} else {
			set = new HashSet<>(set);
		}
		if(set.add(item)) {
			internalSetValue(set);
		}

		if(m_displayMap.containsKey(item))
			return;

		Div label = renderLabel(item);
		SearchAsYouTypeBase<T> input = m_input;
		if(null == input) {
			add(label);
		} else {
			input.appendBeforeMe(label);
		}
		valueHasChanged();
	}


	private void valueHasChanged() throws Exception {
		IValueChanged<EnumSetInput<T>> listener = (IValueChanged<EnumSetInput<T>>) getOnValueChanged();
		if(null != listener) {
			listener.onValueChanged(this);
		}
	}

	private int compareText(T a, T b) {
		try {
			String sa = getLabelText(a);
			String sb = getLabelText(b);
			return sa.compareToIgnoreCase(sb);
		} catch(Exception x) {
			throw WrappedException.wrap(x);				// The idiot that defined this shit stream API should be shot.
		}
	}

	private String getLabelText(T instance) throws Exception {
		Function<T, String> converter = m_converter;
		if(null != converter) {
			return converter.apply(instance);
		}

		Object value = instance;
		String property = m_property;
		if(property != null) {
			PropertyMetaModel<?> pmm = MetaManager.getPropertyMeta(m_actualClass, property);
			value = pmm.getValue(instance);
		}

		//-- Ask the metamodel
		ClassMetaModel cmm = MetaManager.findClassMeta(m_actualClass);
		try {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), value);
			if(null != label)
				return label;
		} catch(Exception x) {
			//-- If not a domain thingy - try others
		}
		return value.toString();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@NonNull public List<T> getData() {
		return m_dataList;
	}

	public EnumSetInput<T> setData(@NonNull List<T> dataList) {
		m_dataList = dataList;
		forceRebuild();
		return this;
	}

	@Nullable public Function<T, String> getConverter() {
		return m_converter;
	}

	public void setConverter(@Nullable Function<T, String> converter) {
		m_converter = converter;
	}

	@Nullable public IRenderInto<T> getRenderer() {
		return m_renderer;
	}

	public EnumSetInput<T> setRenderer(@Nullable IRenderInto<T> renderer) {
		m_renderer = renderer;
		return this;
	}

	public EnumSetInput<T> setMatcher(BiFunction<T, String, Boolean> matcher) {
		m_predicate = matcher;
		return this;
	}

	public boolean isAddSingleMatch() {
		return m_addSingleMatch;
	}

	//public EnumSetInput<T>  setAddSingleMatch(boolean addSingleMatch) {
	//	m_addSingleMatch = addSingleMatch;
	//	SearchAsYouTypeBase<ItemWrapper<T>> input = m_input;
	//	if(null != input)
	//		input.setAddSingleMatch(addSingleMatch);
	//	return this;
	//}

	@Override public void setHint(@Nullable String hintText) {
		setTitle(hintText);
	}
}
