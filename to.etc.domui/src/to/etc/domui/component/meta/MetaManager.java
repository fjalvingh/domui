package to.etc.domui.component.meta;

import java.math.*;
import java.util.*;

import to.etc.domui.component.input.ComboFixed.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * Accessor class to the generalized metadata thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
final public class MetaManager {
	static private List<DataMetaModel>	m_modelList = new ArrayList<DataMetaModel>();
	static private Set<Class<?>>		SIMPLE	= new HashSet<Class<?>>();

	static private Map<Class<?>, DefaultClassMetaModel>		m_classMap = new HashMap<Class<?>, DefaultClassMetaModel>();

	private MetaManager() {
	}

	static synchronized public void		registerModel(DataMetaModel model) {
		List<DataMetaModel>	mm = new ArrayList<DataMetaModel>(m_modelList);
		mm.add(model);
		m_modelList = mm;
	}
	static private synchronized List<DataMetaModel>	getList() {
		if(m_modelList.size() == 0)
			registerModel(new DefaultDataMetaModel());
		return m_modelList;
	}

	static public ClassMetaModel		findClassMeta(Class<?> clz) {
		DefaultClassMetaModel	dmm;
		synchronized(MetaManager.class) {
			dmm = m_classMap.get(clz);
			if(dmm == null) {
				if(clz.getName().contains("$$")) {
					//-- Enhanced class (Hibernate). Get base class instead
					clz = clz.getSuperclass();
					dmm = m_classMap.get(clz);
				}
				if(dmm == null) {
					dmm = new DefaultClassMetaModel(clz);				// Create base class info
					m_classMap.put(clz, dmm);							// Save
				}
			}
		}

		//-- Double lock mechanism externalized to prevent long locks on central metadata table
		synchronized(dmm) {
			if(! dmm.isInitialized()) {
				for(DataMetaModel mm : getList()) {					// Let all providers add their information.
					mm.updateClassMeta(dmm);
				}
				dmm.initialized();
			}
		}
		return dmm;
	}

	static public PropertyMetaModel		findPropertyMeta(Class<?> clz, String name) {
		ClassMetaModel	cm = findClassMeta(clz);
		if(cm == null)
			return null;
		return cm.findProperty(name);
	}

	static public boolean	isSimpleClass(Class<?> clz) {
		return SIMPLE.contains(clz);
	}

	/**
	 * Handles the permission sets like "viewpermission" and "editpermission". If
	 * the array contains null the field can be seen by all users. If it has a value
	 * the first-level array is a set of ORs; the second level are ANDs. Meaning that
	 * an array in the format:
	 * <pre>
	 * { {"admin"}
	 * , {"editroles", "user"}
	 * , {"tester"}
	 * };
	 * </pre>
	 * this means that the field is visible for a user with the roles:
	 * <pre>
	 * 	"admin" OR "tester" OR ("editroles" AND "user")
	 * </pre>
	 *
	 * @param roleset
	 * @param ctx
	 * @return
	 */
	static public boolean	isAccessAllowed(String[][] roleset, RequestContext ctx) {
		if(roleset == null)
			return true;				// No restrictions
		for(String[] orset: roleset) {
			boolean ok = true;
			for(String perm: orset) {
				if(! ctx.hasPermission(perm)) {
					ok = false;
					break;
				}
			}
			//-- Were all "AND" conditions true then accept
			if(ok)
				return true;
		}
		return false;
	}

	static private INodeContentRenderer<?>	createComboLabelRenderer(Class<? extends ILabelStringRenderer<?>> lsr) {
		final ILabelStringRenderer<Object>	lr = (ILabelStringRenderer<Object>)DomApplication.get().createInstance(lsr);
		return new INodeContentRenderer<Object>() {
			public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) {
				String text = lr.getLabelFor(object);
				if(text != null)
					node.setLiteralText(text);
			}
		};
	}

	static private INodeContentRenderer<?>	createComboColumnRenderer(List<DisplayPropertyMetaModel> list) {
		if(list == null || list.size() == 0)
			return null;
		return new DisplayPropertyNodeContentRenderer(list);
	}

	/**
	 * This creates a default combo option value renderer using whatever metadata is available.
	 * @param pmm
	 * @param cmm
	 * @return
	 */
	static public INodeContentRenderer<?>	createDefaultComboRenderer(PropertyMetaModel pmm, ClassMetaModel cmm) {
		//-- Property-level metadata is the 1st choice
		if(pmm != null) {
			if(cmm == null)
				cmm = MetaManager.findClassMeta(pmm.getActualType());

			if(pmm.getComboNodeRenderer() != null)
				return DomApplication.get().createInstance(pmm.getComboNodeRenderer());
			if(pmm.getComboLabelRenderer() != null)
				return createComboLabelRenderer(pmm.getComboLabelRenderer());
			INodeContentRenderer<?>	r = createComboColumnRenderer(pmm.getComboDisplayProperties());
			if(r != null)
				return r;

			//-- No column-based property thingy found. Pass into target class based stuff
		}
		if(cmm != null) {
			if(cmm.getComboNodeRenderer() != null)
				return DomApplication.get().createInstance(cmm.getComboNodeRenderer());
			if(cmm.getComboLabelRenderer() != null)
				return createComboLabelRenderer(cmm.getComboLabelRenderer());
			INodeContentRenderer<?>	r = createComboColumnRenderer(cmm.getComboDisplayProperties());
			if(r != null)
				return r;
		}

		//-- All metadata thingies failed... Render using tostring on the object
		return new INodeContentRenderer<Object>() {
			public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) {
				if(object != null)
					node.setLiteralText(object.toString());
			}
		};
	}

	/**
	 * This is a complex EQUAL routine which compares objects. Each of the objects can be null. Objects
	 * are considered equal when they are the same reference; if a.equal(b) holds or, when the objects
	 * are both objects for which a PK is known, when the PK's are equal.
	 * @param a
	 * @param b
	 * @param cmm
	 * @return
	 */
	static public boolean		areObjectsEqual(Object a, Object b, ClassMetaModel cmm) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		if(a.equals(b))
			return true;

		//-- Try Object key identity, if available
		if(cmm != null) {
			if(cmm.getPrimaryKey() != null) {
				try {
					Object pka = cmm.getPrimaryKey().getAccessor().getValue(a);
					Object pkb = cmm.getPrimaryKey().getAccessor().getValue(b);
					return DomUtil.isEqual(pka, pkb);
				} catch(Exception x) {
					x.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Creates a List of Pair's for each domain value in a class which represents a domain (like an enum or Boolean). The
	 * list is ready to be used by ComboFixed.
	 * @param clz
	 * @return
	 */
	static public <T extends Enum<?>> List<Pair<T>>	createEnumList(Class<T> clz) {
		List<Pair<T>>	res = new ArrayList<Pair<T>>();
		ClassMetaModel	cmm	= MetaManager.findClassMeta(clz);
		Object[]	values = cmm.getDomainValues();
		if(values == null)
			throw new IllegalStateException("The class "+clz+" does not have a discrete list of Domain Values");
		for(Object value: values) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), value);
			if(label == null)
				label = value == null ? "" : value.toString();
			res.add(new Pair<T>((T)value, label));
		}
		return res;
	}

	static public PropertyMetaModel	internalCalculateDottedPath(ClassMetaModel cmm, String name) {
		int pos = name.indexOf('.');				// Dotted name?
		if(pos == -1)
			return cmm.findSimpleProperty(name);	// Use normal resolution directly on the class.

		//-- We must create a synthetic property.
		int	ix	= 0;
		int	len	= name.length();
		ClassMetaModel	ccmm = cmm;					// Current class meta-model for property reached
		List<PropertyMetaModel>	acl = new ArrayList<PropertyMetaModel>(10);
		for(;;) {
			String sub = name.substring(ix, pos);	// Get path component,
			ix	= pos+1;

			PropertyMetaModel	pmm = ccmm.findSimpleProperty(sub);	// Find base property,
			if(pmm == null)
				throw new IllegalStateException("Undefined property '"+sub+"' on classMetaModel="+ccmm);
			acl.add(pmm);							// Next access path,
			ccmm = MetaManager.findClassMeta(pmm.getActualType());

			if(ix >= len)
				break;
			pos	= name.indexOf('.', ix);
			if(pos == -1)
				pos = len;
		}

		//-- Resolved to target. Return a complex proxy.
		return new PathPropertyMetaModel<Object>(acl.toArray(new PropertyMetaModel[acl.size()]));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Expanding properties.								*/
	/*--------------------------------------------------------------*/

	static {
		SIMPLE	= new HashSet<Class<?>>();
		SIMPLE.add(Integer.class);
		SIMPLE.add(Integer.TYPE);
		SIMPLE.add(Long.class);
		SIMPLE.add(Long.TYPE);
		SIMPLE.add(Character.class);
		SIMPLE.add(Character.TYPE);
		SIMPLE.add(Short.class);
		SIMPLE.add(Short.TYPE);
		SIMPLE.add(Byte.class);
		SIMPLE.add(Byte.TYPE);
		SIMPLE.add(Double.class);
		SIMPLE.add(Double.TYPE);
		SIMPLE.add(Float.class);
		SIMPLE.add(Float.TYPE);
		SIMPLE.add(Boolean.class);
		SIMPLE.add(Boolean.TYPE);
		SIMPLE.add(BigDecimal.class);
		SIMPLE.add(String.class);
		SIMPLE.add(BigInteger.class);
	}
}
