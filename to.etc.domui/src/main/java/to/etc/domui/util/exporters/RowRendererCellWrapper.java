package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.tbl.ColumnDef;
import to.etc.domui.converter.IConverter;
import to.etc.domui.dom.html.Div;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.IValueTransformer;
import to.etc.webapp.nls.NlsContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-7-18.
 */
final public class RowRendererCellWrapper<V> implements IExportColumn<V> {
	private final Class<?> m_actualType;

	private final String m_label;

	private final IValueTransformer<V> m_getter;

	private RowRendererCellWrapper(Class<?> actualType, String label, IValueTransformer<V> getter) {
		m_actualType = actualType;
		m_label = label;
		m_getter = getter;
	}

	@Nullable
	static public <I, X, V> RowRendererCellWrapper<V> create(ColumnDef<I, X> c) {
		if(c.isEditable() || c.getControlFactory() != null) {
			return null;
		}
		PropertyMetaModel<Object> pmm = (PropertyMetaModel<Object>) c.getPropertyMetaModel();
		IConverter<Object> converter = (IConverter<Object>) c.getConverter();
		IRenderInto<Object> cr = (IRenderInto<Object>) c.getContentRenderer();

		String label = c.getColumnLabel();
		Class<?> actualType = c.getActualClass();
		IValueTransformer<Object> getter;

		if(null != pmm) {
			if(null == label)
				label = pmm.getDefaultLabel();
			if(null == converter) {
				getter = a -> (V) pmm.getValue(a);
			} else {
				getter = a -> {
					Object value = pmm.getValue(a);
					String s = converter.convertObjectToString(NlsContext.getLocale(), value);
					return (V) s;
				};
				actualType = String.class;
			}

			if(null != cr) {
				actualType = String.class;
				getter = wrapRenderer(getter, cr);
			}

			return new RowRendererCellWrapper<>(actualType, label, (IValueTransformer<V>) getter);
		}

		//-- Instance based thingy...
		if(null == converter) {
			getter = a -> (V) a;
		} else {
			actualType = String.class;
			getter = a -> {
				String s = converter.convertObjectToString(NlsContext.getLocale(), (X) a);
				return (V) s;
			};
		}

		if(null != cr) {
			actualType = String.class;
			getter = wrapRenderer(getter, cr);
		}

		return new RowRendererCellWrapper<>(actualType, label, (IValueTransformer<V>) getter);
	}

	private static IValueTransformer<Object> wrapRenderer(IValueTransformer<Object> getter, IRenderInto<Object> cr) {
		return a -> {
			return convertRender(getter.getValue(a), cr);
		};
	}

	private static <X> String convertRender(X value, IRenderInto<X> cr) throws Exception {
		if(null == value)
			return null;

		Div dv = new Div();
		cr.render(dv, value);
		if(dv.getChildCount() == 0)
			return null;
		return dv.getTextOnly();
	}

	@Override public String getLabel() {
		return m_label;
	}

	@Override public Class<?> getActualType() {
		return m_actualType;
	}

	@Override public IExportCellRenderer<?, ?, ?> getRenderer() {
		return null;
	}

	@Override public V getValue(Object in) throws Exception {
		return m_getter.getValue(in);
	}
}
