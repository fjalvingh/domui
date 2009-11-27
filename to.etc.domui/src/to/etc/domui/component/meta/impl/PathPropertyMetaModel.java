package to.etc.domui.component.meta.impl;

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This is a proxy for an existing PropertyMetaModel for path-based properties. This overrides
 * the Accessor and replaces it with an accessor which walks the path to the target property. In
 * addition this uses extended rules to determine the default label and stuff for the extended
 * property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 29, 2008
 */
public class PathPropertyMetaModel<T> implements PropertyMetaModel, IValueAccessor<T> {
	private PropertyMetaModel m_original;

	/** The full dotted name from the original source to this synthetic property. */
	private String m_dottedName;

	private PropertyMetaModel[] m_accessPath;

	public PathPropertyMetaModel(String dottedName, PropertyMetaModel[] accessPath) {
		m_accessPath = accessPath;
		m_original = accessPath[accessPath.length - 1];
		m_dottedName = dottedName;
	}

	@Override
	public String toString() {
		return "PathProperty[" + m_dottedName + "@" + m_accessPath[0].getClassModel().toString() + "]";
	}

	/**
	 * Calculate the value to get. If any path component is null this returns null, it does not
	 * throw exceptions.
	 *
	 * @see to.etc.domui.util.IValueTransformer#getValue(java.lang.Object)
	 */
	public T getValue(Object in) throws Exception {
		Object cv = in;
		for(PropertyMetaModel pmm : m_accessPath) {
			cv = pmm.getAccessor().getValue(cv);
			if(cv == null)
				return null;
		}
		return (T) cv;
	}

	public void setValue(Object target, T value) throws Exception {
		Object cv = target;
		for(PropertyMetaModel pmm : m_accessPath) {
			if(pmm == m_original) { // Reached last segment?
				//-- Actually set a value now
				((IValueAccessor<T>) pmm.getAccessor()).setValue(cv, value);
				return;
			}

			cv = pmm.getAccessor().getValue(cv);
			if(cv == null)
				throw new IllegalStateException("The property '" + pmm.getName() + " in classModel=" + pmm.getClassModel() + " is null - cannot set a value!!");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides for labels and hints						*/
	/*--------------------------------------------------------------*/
	/*
	 * We use an extended mechanism to determine text resources for pathbased
	 * properties. This allows overriding of remote properties in the reached
	 * class from within the property file of the root source.
	 * The mechanism is as follows: start to locate the full path name starting
	 * from the current location in the class' bundle.
	 */
	/**
	 *
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getDefaultHint()
	 */
	public String getDefaultHint() {
		return locateProperty("hint");
	}

	/**
	 *
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getDefaultLabel()
	 */
	public String getDefaultLabel() {
		//		System.out.println("LOCATE label for " + getName());
		return locateProperty("label");
	}

	/**
	 * Walk the access path to locate a label/hint for the specified property. For
	 * the property a.b.c it must try the following:
	 * <pre>
	 * a.b.c.label, a.b.*.label, a.*.*.label, a.*.label
	 * Then move to bundle for b, and try:
	 * b.c.label, b.*.label
	 * And finally move to c and try c.label.
	 * </pre>
	 *
	 * @param type
	 * @return
	 */
	private String locateProperty(String type) {
		Locale loc = NlsContext.getLocale();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < m_accessPath.length - 1; i++) {
			PropertyMetaModel	pmm = m_accessPath[i];
			BundleRef br = pmm.getClassModel().getClassBundle(); // Current target-path
			String v = attemptProperty(sb, br, i, type, loc);
			if(v != null)
				return v;
		}
		//-- Last resort...
		sb.setLength(0);
		sb.append(m_original.getName());
		sb.append('.');
		sb.append(type);
		String s = m_original.getClassModel().getClassBundle().findMessage(loc, sb.toString());
		if(s != null)
			return s;
		return getName();
	}

	/**
	 * Starting at the specified level, generate all of the pertinent names and try for
	 * a resource string on 'm.
	 *
	 * @param sb
	 * @param br
	 * @param i
	 * @param type
	 * @param loc
	 * @return
	 */
	private String attemptProperty(StringBuilder sb, BundleRef br, int startix, String type, Locale loc) {
		//-- 1. Try a.b.c, a.b.*, a.*.*
		for(int i = m_accessPath.length; i > startix; i--) {
			sb.setLength(0);
			for(int j = startix; j < m_accessPath.length; j++) {
				if(sb.length() > 0)
					sb.append('.');
				if(j >= i)
					sb.append('*');
				else
					sb.append(m_accessPath[j].getName());
			}

			//-- Gotta name...
			sb.append('.');
			sb.append(type);
			String k = sb.toString();
			//			System.out.println("k=" + k);
			String s = br.findMessage(loc, k);
			if(s != null)
				return s;
		}

		//		//-- try a.b.c.d.e, a.b.c.d.*, a.b.c.*.*, a.b.c.*, a.b.*.*, a.b.*,
		//		for(int i = m_accessPath.length; i > startix; i--) {
		//
		//
		//		}
		//

		// TODO

		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Silly proxies.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a compound accessor.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getAccessor()
	 */
	public IValueAccessor< ? > getAccessor() {
		return this;
	}

	public Class< ? > getActualType() {
		return m_original.getActualType();
	}

	public Type getGenericActualType() {
		return m_original.getGenericActualType();
	}

	public ClassMetaModel getClassModel() {
		return m_original.getClassModel();
	}

	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_original.getComboDataSet();
	}

	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_original.getComboDisplayProperties();
	}

	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_original.getComboLabelRenderer();
	}

	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_original.getComboNodeRenderer();
	}

	public String getComponentTypeHint() {
		return m_original.getComponentTypeHint();
	}

	public IConverter< ? > getConverter() {
		return m_original.getConverter();
	}

	public int getDisplayLength() {
		return m_original.getDisplayLength();
	}

	public String getDomainValueLabel(Locale loc, Object val) {
		return m_original.getDomainValueLabel(loc, val);
	}

	public Object[] getDomainValues() {
		return m_original.getDomainValues();
	}

	public String[][] getEditRoles() {
		return m_original.getEditRoles();
	}

	public int getLength() {
		return m_original.getLength();
	}

	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties() {
		return m_original.getLookupFieldDisplayProperties();
	}

	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer() {
		return m_original.getLookupFieldRenderer();
	}

	public String getName() {
		return m_dottedName;
	}

	public int getPrecision() {
		return m_original.getPrecision();
	}

	public YesNoType getReadOnly() {
		return m_original.getReadOnly();
	}

	public PropertyRelationType getRelationType() {
		return m_original.getRelationType();
	}

	public int getScale() {
		return m_original.getScale();
	}

	public SortableType getSortable() {
		return m_original.getSortable();
	}

	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_original.getTableDisplayProperties();
	}

	public TemporalPresentationType getTemporal() {
		return m_original.getTemporal();
	}

	public NumericPresentation getNumericPresentation() {
		return m_original.getNumericPresentation();
	}

	public PropertyMetaValidator[] getValidators() {
		return m_original.getValidators();
	}

	public String[][] getViewRoles() {
		return m_original.getViewRoles();
	}

	public boolean isPrimaryKey() {
		return m_original.isPrimaryKey();
	}

	public boolean isRequired() {
		return m_original.isRequired();
	}
}
