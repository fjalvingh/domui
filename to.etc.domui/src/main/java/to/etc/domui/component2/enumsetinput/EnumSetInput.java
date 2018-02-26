package to.etc.domui.component2.enumsetinput;

import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.component.input.SearchAsYouTypeBase;
import to.etc.domui.component.input.SearchAsYouTypeBase.Result;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * FIXME Functional duplicate of LabelSelector.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-2-18.
 */
@DefaultNonNull
public class EnumSetInput<T> extends AbstractDivControl<Set<T>> {
	private final Class<T> m_actualClass;

	@Nonnull
	private List<T> m_dataList = new ArrayList<>();

	private final Map<T, Div> m_displayMap = new HashMap<>();

	@Nullable
	private Function<T, String> m_converter;

	@Nullable
	private BiFunction<T, String, Boolean> m_predicate;

	@Nullable
	private IRenderInto<T> m_renderer;

	@Nullable
	private SearchAsYouTypeBase<ItemWrapper<T>> m_input;

	private boolean m_addSingleMatch = true;

	public EnumSetInput(Class<T> actualClass) {
		m_actualClass = actualClass;
	}

	public class ItemWrapper<E> {
		final private E m_item;

		final private String m_text;

		public ItemWrapper(E item, String text) {
			m_item = item;
			m_text = text;
		}

		public final String getText() {
			return m_text;
		}

		public E getItem() {
			return m_item;
		}
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
		if(isDisabled() || isReadOnly()) {
			m_input = null;
		} else {
			Class<ItemWrapper<T>> clz = (Class<ItemWrapper<T>>) (Object) ItemWrapper.class;
			SearchAsYouTypeBase<ItemWrapper<T>> input = m_input = new SearchAsYouTypeBase<ItemWrapper<T>>("ui-esic", clz, "text") {
				@Nullable @Override protected List<ItemWrapper<T>> onLookupTyping(String curdata, boolean done) throws Exception {
					return null;
				}

				@Override protected void onEmptyInput(boolean done) throws Exception {
					super.onEmptyInput(done);
				}

				@Override protected void onRowSelected(ItemWrapper<T> value) throws Exception {

				}
			};
			add(input);
			input.setAddSingleMatch(isAddSingleMatch());
			input.setCssClass("ui-esic-input");
			//input.setHandler(new ITypingListener<ItemWrapper<T>>() {
			//	@Override public Result<ItemWrapper<T>> queryFromString(String input, int max) throws Exception {
			//		return searchItemsBy(input, max);
			//	}
			//
			//	@Override public void onSelect(ItemWrapper<T> instance) throws Exception {
			//		addItem(instance.getItem());
			//	}
			//
			//	@Override public void onEnter(String value) throws Exception {
			//
			//	}
			//});
		}
	}

	/**
	 * Try to find the value(s) matching.
	 */
	private Result<ItemWrapper<T>> searchItemsBy(String input, int max) {
		List<T> data = getData();
		if(null == data)
			return new Result<>(Collections.emptyList(), null);
		input = input.toLowerCase();
		List<ItemWrapper<T>> res = new ArrayList<>();
		BiFunction<T, String, Boolean> predicate = m_predicate;
		ItemWrapper<T> exact = null;
		for(T item : data) {
			if(null == predicate) {
				String text = getLabelText(item);
				if(text.toLowerCase().contains(input)) {
					ItemWrapper<T> itemWrapper = new ItemWrapper<>(item, text);
					res.add(itemWrapper);
					if(text.equalsIgnoreCase(input))
						exact = itemWrapper;
					if(res.size() >= max) {
						break;
					}
				}
			} else if(predicate.apply(item, input)) {
				res.add(new ItemWrapper<>(item, getLabelText(item)));
				if(res.size() >= max) {
					break;
				}
			}
		}
		return new Result<>(res, exact);
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
		Button delBtn = new Button().css("ui-esic-del");
		label.add(delBtn);
		delBtn.add(new FaIcon(FaIcon.faTimes));
		delBtn.setClicked(a -> {
			removeItem(value);
		});
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


	private void addItem(T item) throws Exception {
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
		SearchAsYouTypeBase<ItemWrapper<T>> input = m_input;
		if(null == input) {
			add(label);
		} else {
			input.appendBeforeMe(label);
		}
	}


	private void valueHasChanged() throws Exception {
		IValueChanged<EnumSetInput<T>> listener = (IValueChanged<EnumSetInput<T>>) getOnValueChanged();
		if(null != listener) {
			listener.onValueChanged(this);
		}
	}

	private int compareText(T a, T b) {
		String sa = getLabelText(a);
		String sb = getLabelText(b);
		return sa.compareToIgnoreCase(sb);
	}

	private String getLabelText(T instance) {
		Function<T, String> converter = m_converter;
		if(null != converter) {
			return converter.apply(instance);
		}
		//-- Ask the metamodel
		ClassMetaModel cmm = MetaManager.findClassMeta(m_actualClass);
		try {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), instance);
			if(null != label)
				return label;
		} catch(Exception x) {
			//-- If not a domain thingy - try others
		}
		return instance.toString();
	}


	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@Nonnull public List<T> getData() {
		return m_dataList;
	}

	public EnumSetInput<T> setData(@Nonnull List<T> dataList) {
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

	public EnumSetInput<T>  setAddSingleMatch(boolean addSingleMatch) {
		m_addSingleMatch = addSingleMatch;
		SearchAsYouTypeBase<ItemWrapper<T>> input = m_input;
		if(null != input)
			input.setAddSingleMatch(addSingleMatch);
		return this;
	}
}
