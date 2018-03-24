package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.tbl.ColumnDefList;
import to.etc.domui.component.tbl.SimpleColumnDef;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.IValueTransformer;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import java.util.List;

public class DefaultPopupRowRenderer<T> implements IRenderInto<T> {
	@Nonnull
	final private ColumnDefList<T> m_columnList;

	public DefaultPopupRowRenderer(@Nonnull ClassMetaModel cmm) {
		m_columnList = new ColumnDefList<T>((Class<T>) cmm.getActualClass(), cmm);
		m_columnList.addDefaultColumns();
	}

	public DefaultPopupRowRenderer(@Nonnull ClassMetaModel cmm, List<String> columns) {
		m_columnList = new ColumnDefList<T>((Class<T>) cmm.getActualClass(), cmm);
		for(String column : columns) {
			m_columnList.column(column);
		}
	}


	@Override
	public void render(NodeContainer node, T instance) throws Exception {
		int column = 0;
		for(final SimpleColumnDef< ? > cd : m_columnList) {
			renderColumn(node, column++, instance, cd);
		}
	}

	private <X> void renderColumn(@Nonnull NodeContainer node, int column, @Nonnull T instance, @Nonnull SimpleColumnDef<X> cd) throws Exception {
		//-- If a value transformer is known get the column value, else just use the instance itself (case when Renderer is used)
		X colval;
		IValueTransformer< ? > vtr = cd.getValueTransformer();
		if(vtr == null)
			colval = (X) instance;
		else
			colval = (X) vtr.getValue(instance);


		//-- Is a node renderer used?
		IRenderInto< ? > contentRenderer = cd.getContentRenderer();
		if(null != contentRenderer) {
			if(column > 0) {
				node.add(" ");
			}
			((IRenderInto<Object>) contentRenderer).renderOpt(node, colval);
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				IObjectToStringConverter<X> presentationConverter = cd.getPresentationConverter();
				if(presentationConverter != null)
					s = presentationConverter.convertObjectToString(NlsContext.getLocale(), colval);
				else
					s = String.valueOf(colval);
			}
			if(s != null) {
				if(column > 0) {
					node.add(" ");
				}
				node.add(s);
			}
		}
	}
}
