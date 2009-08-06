package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * A special property consisting of a list of joined properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class JoinedDisplayProperty extends ExpandedDisplayProperty implements IValueAccessor<String> {
	private List<DisplayPropertyMetaModel> m_displayList;

	private List<PropertyMetaModel> m_propertyList;

	public JoinedDisplayProperty(List<DisplayPropertyMetaModel> list, List<PropertyMetaModel> plist, IValueAccessor< ? > acc) {
		super(null, null, acc);
		m_displayList = list;
		m_propertyList = plist;
		if(list.size() < 2)
			throw new IllegalStateException("?? Expecting at least 2 properties in a joined display property.");

		//-- Calculate basics..
		DisplayPropertyMetaModel dpm = list.get(0); // 1st thingy contains sizes,
		setDisplayLength(dpm.getDisplayLength());
		setActualType(String.class);
		setSortable(SortableType.UNSORTABLE);
		setName(dpm.getName());
	}

	@Override
	public IValueAccessor<String> getAccessor() {
		return this;
	}

	public void setValue(Object target, String value) throws Exception {
		throw new IllegalStateException("You cannot set a joined display property.");
	}

	/**
	 * This creates the joined value of the items in the set.
	 * @see to.etc.domui.util.IValueTransformer#getValue(java.lang.Object)
	 */
	public String getValue(Object in) throws Exception {
		Object root = super.getAccessor().getValue(in); // Obtain the root object
		if(root == null)
			return null;

		//-- Now access using the root value
		StringBuilder sb = new StringBuilder();
		String join = null;
		for(int i = 0; i < m_displayList.size(); i++) {
			DisplayPropertyMetaModel dpm = m_displayList.get(i);
			PropertyMetaModel pm = m_propertyList.get(i);
			Object value = pm.getAccessor().getValue(root);
			if(value == null)
				continue;
			String s = ConverterRegistry.convertValueToString((Class<? extends IConverter<Object>>) getConverterClass(), value);
			if(s == null || s.length() == 0)
				continue;
			if(join != null)
				sb.append(join);
			join = dpm.getJoin();
			sb.append(s);
		}
		return sb.toString();
	}

	@Override
	public String getDefaultLabel() {
		DisplayPropertyMetaModel dm = m_displayList.get(0);
		String lbl = dm.getLabel(); // Is the label overridden in the DisplayProperty?
		if(lbl != null)
			return lbl;
		return m_propertyList.get(0).getDefaultLabel();
	}
}
