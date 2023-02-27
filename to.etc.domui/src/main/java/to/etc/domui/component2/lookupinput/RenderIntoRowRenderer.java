package to.etc.domui.component2.lookupinput;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.tbl.ColumnDef;
import to.etc.domui.component.tbl.ColumnList;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConverter;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.query.QField;

import java.util.Locale;

@NonNullByDefault
public class RenderIntoRowRenderer<T> implements IRenderInto<T> {

	private final Class<T> m_dataClass;

	private final ClassMetaModel m_metaModel;

	private final ColumnList<T> m_columnList;

	public RenderIntoRowRenderer(@NonNull Class<T> data, QField<T, ?>... props) {
		this(data, MetaManager.findClassMeta(data), props);
	}

	public RenderIntoRowRenderer(@NonNull Class<T> data, @NonNull ClassMetaModel cmm, QField<T, ?>... props) {
		m_dataClass = data;
		m_metaModel = cmm;
		m_columnList = new ColumnList<T>(data, m_metaModel);
		for(QField<T, ?> prop: props) {
			getColumnList().column(prop);
		}
	}

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		getColumnList().addDefaultColumns();
	}

	@NonNull
	public <V> ColumnDef<T, V> column(QField<T, V> field) {
		return getColumnList().column(field);
	}

	@NonNull
	public ColumnList<T> getColumnList() {
		return m_columnList;
	}

	@Override
	public void render(@NonNull NodeContainer node, @NonNull T instance) throws Exception {
		Div row = node.add(new Div());
		row.setDisplay(DisplayType.FLEX);
		for(int colIndex = 0; colIndex < getColumnList().size(); colIndex++) {
			ColumnDef<T, ?> cd = getColumnList().get(colIndex);
			Span cell = row.add(new Span());
			if(colIndex > 0) {
				cell.setMarginLeft("4px");
			}
			renderColumn(cell, colIndex, instance, cd);
		}
	}

	private <X> void renderColumn(@NonNull NodeContainer node, int column, @NonNull T instance, @NonNull ColumnDef<T, X> cd) throws Exception {

		String cssClass = cd.getCssClass();
		if(cssClass != null) {
			node.addCssClass(cssClass);
		}

		if(cd.isNowrap())
			node.setDisplay(DisplayType.INLINE_BLOCK);

		if(cd.getAlign() != null)
			node.setTextAlign(cd.getAlign());
		else if(cssClass != null) {
			node.addCssClass(cssClass);
		}

		X value = cd.getColumnValue(instance);

		if(null == value) {
			return;
		}

		//-- Render the value, in whatever way. The value is bound to the model so that updates cause a render.
		IRenderInto<X> contentRenderer = cd.getContentRenderer();
		IConverter<X> cellConverter = cd.getConverter();
		PropertyMetaModel<X> pmm = cd.getPropertyMetaModel();
		if(null != contentRenderer) {
			contentRenderer.render(node, value);
		}else {
			if(pmm != null) {
				//-- Bind the property to a display control.
				if(null == cellConverter) {
					cellConverter = ConverterRegistry.findBestConverter(pmm);
				}
			}
			if(null != cellConverter) {
				node.add(cellConverter.convertObjectToString(Locale.getDefault(), value));
			}else {
				node.add(value.toString());
			}
		}
	}

}
