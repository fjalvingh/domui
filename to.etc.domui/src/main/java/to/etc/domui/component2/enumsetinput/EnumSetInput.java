package to.etc.domui.component2.enumsetinput;

import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.converter.IConverter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
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
	private IConverter<T> m_converter;

	@Nullable
	private IRenderInto<T> m_renderer;

	public EnumSetInput(Class<T> actualClass) {
		m_actualClass = actualClass;
	}

	@Override public void createContent() throws Exception {
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
		IConverter<T> converter = m_converter;
		if(null != converter) {
			return converter.convertObjectToString(NlsContext.getLocale(), instance);
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

	@Nullable public IConverter<T> getConverter() {
		return m_converter;
	}

	public EnumSetInput<T> setConverter(@Nullable IConverter<T> converter) {
		m_converter = converter;
		return this;
	}

	@Nullable public IRenderInto<T> getRenderer() {
		return m_renderer;
	}

	public EnumSetInput<T> setRenderer(@Nullable IRenderInto<T> renderer) {
		m_renderer = renderer;
		return this;
	}
}
