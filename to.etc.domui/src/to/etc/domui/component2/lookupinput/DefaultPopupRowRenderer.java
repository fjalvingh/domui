package to.etc.domui.component2.lookupinput;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class DefaultPopupRowRenderer<T> implements INodeContentRenderer<T> {
	@Nonnull
	final private ColumnDefList<T> m_columnList;

	public DefaultPopupRowRenderer(@Nonnull ClassMetaModel cmm) {
		m_columnList = new ColumnDefList<T>((Class<T>) cmm.getActualClass(), cmm);
		m_columnList.addDefaultColumns();
	}

	@Override
	public void renderNodeContent(NodeBase component, NodeContainer node, T instance, Object parameters) throws Exception {
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
		INodeContentRenderer< ? > contentRenderer = cd.getContentRenderer();
		if(null != contentRenderer) {
			if(column > 0) {
				node.add(" ");
			}
			((INodeContentRenderer<Object>) contentRenderer).renderNodeContent(node, node, colval, instance);
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				IObjectToStringConverter<X> presentationConverter = cd.getPresentationConverter();
				if(presentationConverter != null)
					s = ((IConverter<X>) presentationConverter).convertObjectToString(NlsContext.getLocale(), colval);
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
