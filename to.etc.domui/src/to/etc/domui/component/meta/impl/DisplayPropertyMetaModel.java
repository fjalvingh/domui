package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

public class DisplayPropertyMetaModel extends BasicPropertyMetaModel {
	private String				m_name;
	private String				m_join;

	public DisplayPropertyMetaModel() {
	}
	public DisplayPropertyMetaModel(MetaDisplayProperty p) {
		m_name = p.name();
		setLabel(Constants.NO_DEFAULT_LABEL.equals(p.defaultLabel()) ? null : p.defaultLabel());
		setConverterClass( p.converterClass() == IConverter.class ? null : p.converterClass());
		setSortable( p.defaultSortable());
		setDisplayLength( p.displayLength() );
		setReadOnly(p.readOnly());
		m_join				= p.join().equals(Constants.NO_JOIN) ? null : p.join();
	}

	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name;
	}
	public String getJoin() {
		return m_join;
	}
	public void setJoin(String join) {
		m_join = join;
	}
	static public List<DisplayPropertyMetaModel>	decode(MetaDisplayProperty[] mar) {
		List<DisplayPropertyMetaModel>	list = new ArrayList<DisplayPropertyMetaModel>(mar.length);
		for(MetaDisplayProperty p : mar) {
			list.add(new DisplayPropertyMetaModel(p));
		}
		return list;
	}

	/**
	 * Returns the attribute as a string value.
	 * @param root
	 * @return
	 */
	public String	getAsString(Object root) throws Exception {
		Object	value	= DomUtil.getPropertyValue(root, getName());
		if(getConverterClass() != null)
			return ConverterRegistry.convertValueToString(getConverterClass(), value);
		return value == null ? "" : value.toString();
	}
}
