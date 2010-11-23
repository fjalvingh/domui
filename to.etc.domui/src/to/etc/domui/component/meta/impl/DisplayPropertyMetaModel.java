package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * Implementation for a Display Property metamodel. The Display Property data overrides the default
 * metadata for a property in a given display context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2009
 */
public class DisplayPropertyMetaModel extends BasicPropertyMetaModel {
	private String m_name;

	private String m_join;

	private String m_renderHint;

	/**
	 * This is the model of the class that these are <i>in</i>. That is <i>not</i> always the class those properties must be resolved on - that is the <i>target</i> class.
	 */
	private ClassMetaModel m_containedInClass;

	private String m_labelKey;

	public DisplayPropertyMetaModel() {}

	@SuppressWarnings("unchecked")
	public DisplayPropertyMetaModel(ClassMetaModel cmm, MetaDisplayProperty p) {
		m_containedInClass = cmm;
		m_name = p.name();
		if(!Constants.NO_DEFAULT_LABEL.equals(p.defaultLabel()))
			m_labelKey = p.defaultLabel();
		//		setConverter((p.converterClass() == DummyConverter.class ? null : ConverterRegistry.getConverterInstance(p.converterClass())));
		// 20091123 This kludge below is needed because otherwise the JDK compiler pukes on this generics abomination.
		IConverter< ? > c = null;
		if(p.converterClass() != DummyConverter.class)
			c = ConverterRegistry.getConverterInstance((Class< ? extends IConverter< ? >>) p.converterClass());
		setConverter(c);
		setSortable(p.defaultSortable());
		setDisplayLength(p.displayLength());
		setReadOnly(p.readOnly());
		m_join = p.join().equals(Constants.NO_JOIN) ? null : p.join();
		setRenderHint(p.renderHint());
	}

	/**
	 * Converts a list of MetaDisplayProperty annotations into their metamodel equivalents.
	 * @param cmm
	 * @param mar
	 * @return
	 */
	static public List<DisplayPropertyMetaModel> decode(ClassMetaModel cmm, MetaDisplayProperty[] mar) {
		List<DisplayPropertyMetaModel> list = new ArrayList<DisplayPropertyMetaModel>(mar.length);
		for(MetaDisplayProperty p : mar) {
			list.add(new DisplayPropertyMetaModel(cmm, p));
		}
		return list;
	}

	/**
	 * Returns the property name this pertains to. This can be a property path expression.
	 * @return
	 */
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	/**
	 * If this is joined display property, this returns the string to put between the joined values. Returns
	 * null for unjoined properties.
	 * @return
	 */
	public String getJoin() {
		return m_join;
	}

	public void setJoin(String join) {
		m_join = join;
	}

	/**
	 * If the label for this display property is overridden this returns the value (not the key) for
	 * the overridden label. If this display property does not override the label it returns null. When
	 * the key does not exist in the bundle this returns the key error string (???+key+???).
	 * @return
	 */
	public String getLabel() {
		if(m_labelKey == null)
			return null;

		return m_containedInClass.getClassBundle().getString(m_labelKey);
	}

	/**
	 * Returns the attribute as a string value.
	 * @param root
	 * @return
	 */
	public <X, T extends IConverter<X>> String getAsString(Object root) throws Exception {
		Object value = DomUtil.getPropertyValue(root, getName());
		if(getConverter() != null)
			return ((T) getConverter()).convertObjectToString(NlsContext.getLocale(), (X) value);
		return value == null ? "" : value.toString();
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(String renderHint) {
		m_renderHint = renderHint;
	}

	@Override
	public String toString() {
		return "DisplayPropertyMetaModel[" + getName() + "]";
	}
}
