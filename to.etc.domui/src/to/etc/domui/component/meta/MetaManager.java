/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.meta;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

/**
 * Accessor class to the generalized metadata thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
final public class MetaManager {
	static private List<IClassMetaModelFactory> m_modelList = new ArrayList<IClassMetaModelFactory>();

	/**
	 * Mapped lock object referring to a ClassMetaModel instance, which can be in initialization.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jun 1, 2010
	 */
	static private final class MRef {
		private ClassMetaModel m_classModel;

		public MRef() {}

		public ClassMetaModel getClassModel() {
			return m_classModel;
		}

		public void setClassModel(ClassMetaModel classModel) {
			m_classModel = classModel;
		}
	}

//	static private Set<Class< ? >> SIMPLE = new HashSet<Class< ? >>();

	/**
	 * Map indexed by Class<?> or IMetaClass returning the classmodel for that instance.
	 */
	static private Map<Object, MRef> m_classMap = new HashMap<Object, MRef>();

	private MetaManager() {}

	static synchronized public void registerModel(@Nonnull IClassMetaModelFactory model) {
		List<IClassMetaModelFactory> mm = new ArrayList<IClassMetaModelFactory>(m_modelList);
		mm.add(model);
		m_modelList = mm;
	}

	@Nonnull
	static private synchronized List<IClassMetaModelFactory> getList() {
		if(m_modelList.size() == 0)
			registerModel(new DefaultJavaClassMetaModelFactory());
		return m_modelList;
	}

	@Nonnull
	static public ClassMetaModel findClassMeta(@Nonnull Class< ? > clz) {
		if(clz == null)
			throw new IllegalArgumentException("Class<?> parameter cannot be null");

		MRef ref;
		List<IClassMetaModelFactory> list;
		synchronized(MetaManager.class) {
			list = getList();
			ref = m_classMap.get(clz);
			if(ref == null) {
				if(clz.getName().contains("$$")) {
					//-- Enhanced class (Hibernate). Get base class instead
					clz = clz.getSuperclass();
					ref = m_classMap.get(clz);
				}
				if(ref == null) {
					ref = new MRef();
					m_classMap.put(clz, ref); // Save
				}
			}
		}
		initializeModel(ref, clz, list);
		return ref.getClassModel();
	}

	/**
	 * Get the metamodel for some metadata-defined object.
	 * @param mc
	 * @return
	 */
	@Nonnull
	static public ClassMetaModel findClassMeta(@Nonnull IMetaClass mc) {
		//-- If the IMetaClass itself is a model- just use it, without caching.
		if(mc instanceof ClassMetaModel)
			return (ClassMetaModel) mc;
		if(mc == null)
			throw new IllegalArgumentException("IMetaClass parameter cannot be null");

		//-- We need some factory to create it.
		MRef ref;
		List<IClassMetaModelFactory> list;
		synchronized(MetaManager.class) {
			list = getList();
			ref = m_classMap.get(mc);
			if(ref == null) {
				ref = new MRef();
				m_classMap.put(mc, ref); // Save
			}
		}
		initializeModel(ref, mc, list);
		return ref.getClassModel();
	}

	/**
	 * Walk all factories and let one of them create the class model for this thingy. If all fail abort.
	 * @param ref
	 * @param modelList
	 */
	private static void initializeModel(@Nonnull MRef ref, @Nonnull Object theThingy, @Nonnull List<IClassMetaModelFactory> modelList) {
		synchronized(ref) {
			if(ref.getClassModel() == null) {
				/*
				 * We need to find a factory that knows how to deliver this metadata.
				 */
				int bestscore = 0;
				int hitct = 0;
				IClassMetaModelFactory best = null;
				for(IClassMetaModelFactory mmf : modelList) {
					int score = mmf.accepts(theThingy);
					if(score > 0) {
						if(score == bestscore)
							hitct++;
						else if(score > bestscore) {
							bestscore = score;
							best = mmf;
							hitct = 1;
						}
					}
				}

				//-- We MUST have some factory now, or we're in trouble.
				if(best == null)
					throw new IllegalStateException("No IClassModelFactory accepts the type '" + theThingy + "', which is a " + theThingy.getClass());
				if(hitct > 1)
					throw new IllegalStateException("Two IClassModelFactory's accept the type '" + theThingy + "' (which is a " + theThingy.getClass() + ") at score=" + bestscore);

				//-- Acceptable. Let it create the model.
				ClassMetaModel cmm = best.createModel(theThingy);
				if(cmm == null)
					throw new IllegalStateException("The IClassModelFactory " + best + " did not create a ClassMetaModel for '" + theThingy + "' (which is a " + theThingy.getClass() + ")");
				ref.setClassModel(cmm); // Marks as initialized.
			}
		}
	}

	/**
	 * Find a property using the metamodel for a class. Returns null if not found.
	 * @param clz
	 * @param name
	 * @return
	 */
	@Nullable
	static public PropertyMetaModel< ? > findPropertyMeta(Class< ? > clz, String name) {
		ClassMetaModel cm = findClassMeta(clz);
		return cm.findProperty(name);
	}

	/**
	 * Find a property using some genericized meta definition. Returns null if not found.
	 * @param mc
	 * @param name
	 * @return
	 */
	@Nullable
	static public PropertyMetaModel< ? > findPropertyMeta(IMetaClass mc, String name) {
		ClassMetaModel cm = findClassMeta(mc);
		return cm.findProperty(name);
	}

	@Nonnull
	static public PropertyMetaModel< ? > getPropertyMeta(Class< ? > clz, String name) {
		PropertyMetaModel< ? > pmm = findPropertyMeta(clz, name);
		if(pmm == null)
			throw new ProgrammerErrorException("The property '" + clz.getName() + "." + name + "' is not known.");
		return pmm;
	}

	@Nonnull
	static public PropertyMetaModel< ? > getPropertyMeta(IMetaClass clz, String name) {
		PropertyMetaModel< ? > pmm = findPropertyMeta(clz, name);
		if(pmm == null)
			throw new ProgrammerErrorException("The property '" + clz + "." + name + "' is not known.");
		return pmm;
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
	static public boolean isAccessAllowed(String[][] roleset, IRequestContext ctx) {
		if(roleset == null)
			return true; // No restrictions
		for(String[] orset : roleset) {
			boolean ok = true;
			for(String perm : orset) {
				if(!ctx.hasPermission(perm)) {
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

	static private INodeContentRenderer< ? > createComboLabelRenderer(Class< ? extends ILabelStringRenderer< ? >> lsr) {
		final ILabelStringRenderer<Object> lr = (ILabelStringRenderer<Object>) DomApplication.get().createInstance(lsr);
		return new INodeContentRenderer<Object>() {
			@Override
			public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) {
				String text = lr.getLabelFor(object);
				if(text != null)
					node.setText(text);
			}
		};
	}

	static private boolean hasDisplayProperties(List<DisplayPropertyMetaModel> list) {
		return list != null && list.size() > 0;
	}

	static private INodeContentRenderer< ? > TOSTRING_RENDERER = new INodeContentRenderer<Object>() {
		@Override
		public void renderNodeContent(NodeBase component, NodeContainer node, Object object, Object parameters) {
			if(object != null)
				node.setText(object.toString());
		}
	};


	/**
	 * This creates a default combo option value renderer using whatever metadata is available.
	 * @param pmm	If not-null this takes precedence. This then <b>must</b> be the property that
	 * 				is to be filled from the list-of-values in the combo. The property is used
	 * 				to override the presentation only. Formally speaking, pmm.getActualType() must
	 * 				be equal to the combo's list item type, and the renderer expects items of that
	 * 				type.
	 *
	 * @param cmm
	 * @return
	 */
	@Nonnull
	static public INodeContentRenderer< ? > createDefaultComboRenderer(@Nullable PropertyMetaModel< ? > pmm, @Nullable ClassMetaModel cmm) {
		//-- Property-level metadata is the 1st choice
		if(pmm != null) {
			cmm = MetaManager.findClassMeta(pmm.getActualType()); // Always use property's class model.

			if(pmm.getComboNodeRenderer() != null)
				return DomApplication.get().createInstance(pmm.getComboNodeRenderer());
			if(pmm.getComboLabelRenderer() != null)
				return createComboLabelRenderer(pmm.getComboLabelRenderer());

			/*
			 * Check for Display properties on both the property itself or on the target class, and use the 1st one
			 * found. The names in these properties refer to the names in the /target/ class,
			 * so they do not contain the name of "this" property; so we need the classmodel of the target type and
			 * the idempotent accessor.
			 */
			List<DisplayPropertyMetaModel> dpl = pmm.getComboDisplayProperties(); // From property;
			if(!hasDisplayProperties(dpl))
				dpl = cmm.getComboDisplayProperties(); // Try the class then;
			if(hasDisplayProperties(dpl)) {
				/*
				 * The property has DisplayProperties.
				 */
				List<ExpandedDisplayProperty< ? >> xpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, cmm, null);
				return new DisplayPropertyNodeContentRenderer(cmm, xpl);
			}
			return TOSTRING_RENDERER; // Just tostring it..
		}

		/*
		 * If a ClassMetaModel is present this means the value object will have that ClassMetaModel.
		 */
		if(cmm != null) {
			if(cmm.getComboNodeRenderer() != null)
				return DomApplication.get().createInstance(cmm.getComboNodeRenderer());
			if(cmm.getComboLabelRenderer() != null)
				return createComboLabelRenderer(cmm.getComboLabelRenderer());

			if(hasDisplayProperties(cmm.getComboDisplayProperties())) {
				/*
				 * The value class has display properties; expand them. Since this is the value class we need no root accessor (== identity/same accessor).
				 */
				List<ExpandedDisplayProperty< ? >> xpl = ExpandedDisplayProperty.expandDisplayProperties(cmm.getComboDisplayProperties(), cmm, null);
				return new DisplayPropertyNodeContentRenderer(cmm, xpl);
			}
		}
		return TOSTRING_RENDERER; // Just tostring it..
	}

	/**
	 * This is a complex EQUAL routine which compares objects. Each of the objects can be null. Objects
	 * are considered equal when they are the same reference; if a.equal(b) holds or, when the objects
	 * are both objects for which a PK is known, when the PK's are equal.
	 * Also works for array types.
	 * @param a
	 * @param b
	 * @param cmm
	 * @return
	 */
	static public boolean areObjectsEqual(Object a, Object b, ClassMetaModel cmm) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		if(a.equals(b))
			return true;

		//-- Classes must be the same type but we allow for proxying
		Class< ? > acl = a.getClass();
		Class< ? > bcl = b.getClass();
		if(!acl.isAssignableFrom(bcl) && !bcl.isAssignableFrom(acl))
			return false;

		//-- Try Object key identity, if available
		if(cmm == null)
			cmm = findClassMeta(a.getClass());
		if(cmm.getPrimaryKey() != null) {
			try {
				//Common case is to compare data items of different types - i.e. in rendering of combo with items of different types.
				//To prevent unnecessary exception logs, we have to use right class meta for both arguments
				ClassMetaModel acmm;
				ClassMetaModel bcmm;
				if(acl != bcl) {
					acmm = findClassMeta(acl);
					bcmm = findClassMeta(bcl);
					if(acmm.getPrimaryKey() == null || bcmm.getPrimaryKey() == null) {
						return false;
					}
				} else {
					acmm = cmm;
					bcmm = cmm;
				}
				Object pka = acmm.getPrimaryKey().getValue(a);
				Object pkb = bcmm.getPrimaryKey().getValue(b);
				return DomUtil.isEqual(pka, pkb);
			} catch(Exception x) {
				x.printStackTrace();
				return false;
			}
		}
		//-- We need a special handlings for arrays, since built-in equals does not work for arrays!
		if(a.getClass().isArray()) {
			if(Array.getLength(a) != Array.getLength(b)) {
				return false;
			}
			for(int i = Array.getLength(a); --i >= 0;) {
				if(!areObjectsEqual(Array.get(a, i), Array.get(b, i), findClassMeta(acl.getComponentType()))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	static public boolean areObjectsEqual(Object a, Object b) {
		return areObjectsEqual(a, b, null);
	}

	/**
	 * Locate the enum's default label.
	 * @param <T>
	 * @param val
	 * @return
	 */
	static public <T extends Enum< ? >> String findEnumLabel(T val) {
		if(val == null)
			return null;
		ClassMetaModel cmm = findClassMeta(val.getClass());
		return cmm.getDomainLabel(NlsContext.getLocale(), val);
	}


	/**
	 * Creates a List of Pair's for each domain value in a class which represents a domain (like an enum or Boolean). The
	 * list is ready to be used by ComboFixed.
	 * @param clz
	 * @return
	 */
	static public <T extends Enum< ? >> List<ValueLabelPair<T>> createEnumList(Class<T> clz) {
		List<ValueLabelPair<T>> res = new ArrayList<ValueLabelPair<T>>();
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		Object[] values = cmm.getDomainValues();
		if(values == null)
			throw new IllegalStateException("The class " + clz + " does not have a discrete list of Domain Values");
		for(Object value : values) {
			String label = cmm.getDomainLabel(NlsContext.getLocale(), value);
			if(label == null)
				label = value == null ? "" : value.toString();
			res.add(new ValueLabelPair<T>((T) value, label));
		}
		return res;
	}

	static public PropertyMetaModel< ? > internalCalculateDottedPath(ClassMetaModel cmm, String name) {
		int pos = name.indexOf('.'); // Dotted name?
		if(pos == -1)
			return cmm.findSimpleProperty(name); // Use normal resolution directly on the class.

		//-- We must create a synthetic property.
		int ix = 0;
		int len = name.length();
		ClassMetaModel ccmm = cmm; // Current class meta-model for property reached
		List<PropertyMetaModel< ? >> acl = new ArrayList<PropertyMetaModel< ? >>(10);
		for(;;) {
			String sub = name.substring(ix, pos); // Get path component,
			ix = pos + 1;

			PropertyMetaModel< ? > pmm = ccmm.findSimpleProperty(sub); // Find base property,
			if(pmm == null)
				throw new IllegalStateException("Invalid property path '" + name + "' on " + cmm + ": property '" + sub + "' on classMetaModel=" + ccmm + " does not exist");
			acl.add(pmm); // Next access path,
			ccmm = MetaManager.findClassMeta(pmm.getActualType());

			if(ix >= len)
				break;
			pos = name.indexOf('.', ix);
			if(pos == -1)
				pos = len;
		}

		//-- Resolved to target. Return a complex proxy.
		return new PathPropertyMetaModel<Object>(name, acl.toArray(new PropertyMetaModel[acl.size()]));
	}

	/**
	 * Parse the property path and return the list of properties in the path. This explicitly allows
	 * traversing child relations provided generic type information is present to denote the child's type.
	 * @param m
	 * @param compoundName
	 * @return
	 */
	static public List<PropertyMetaModel< ? >> parsePropertyPath(@Nonnull ClassMetaModel m, String compoundName) {
		int ix = 0;
		int len = compoundName.length();
		List<PropertyMetaModel< ? >> res = new ArrayList<PropertyMetaModel< ? >>();
		ClassMetaModel cmm = m;
		while(ix < len) {
			int pos = compoundName.indexOf('.', ix);
			String name;
			if(pos == -1) {
				name = compoundName.substring(ix); // Last segment
				ix = len;
			} else {
				name = compoundName.substring(ix, pos); // Intermediary segment,
				ix = pos + 1;
			}

			if(null == cmm)
				throw new IllegalStateException("Metamodel got null while parsing " + compoundName);
			PropertyMetaModel< ? > pmm = cmm.findSimpleProperty(name);
			if(pmm == null)
				throw new MetaModelException(Msgs.BUNDLE, Msgs.MM_COMPOUND_PROPERTY_NOT_FOUND, compoundName, name, cmm.toString());

			//-- If this is a child property it represents some collection; use the collection's type as next thing.
			if(pmm.getRelationType() == PropertyRelationType.DOWN || Collection.class.isAssignableFrom(pmm.getActualType()) || pmm.getActualType().isArray()) {
				ClassMetaModel nextmm = null;

				//-- This must be some kind of collectable subtype or we're in sh*t.
				Type vtype = pmm.getGenericActualType();
				if(vtype != null) {
					Class< ? > vclass = findCollectionType(vtype); // Try to get value class type.
					if(vclass != null)
						nextmm = findClassMeta(vclass);
				}
				if(nextmm == null && ix >= len)
					throw new MetaModelException(Msgs.BUNDLE, Msgs.MM_UNKNOWN_COLLECTION_TYPE, compoundName, name, vtype);
				cmm = nextmm;
			} else {
				cmm = MetaManager.findClassMeta(pmm.getActualType());
			}
			res.add(pmm);
		}
		return res;
	}

	/**
	 * This tries to determine the value class for a property defined as some kind
	 * of Collection&lt;T&gt; or T[]. If the type cannot be determined this returns
	 * null.
	 *
	 * @param genericType
	 * @return
	 */
	static public Class< ? > findCollectionType(Type genericType) {
		if(genericType instanceof Class<?>) {
			Class<?> cl = (Class<?>) genericType;
			if(cl.isArray()) {
				return cl.getComponentType();
			}
		}
		if(genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			Type raw = pt.getRawType();

			//-- This must be a collection type of class.
			if(raw instanceof Class< ? >) {
				Class< ? > cl = (Class< ? >) raw;
				if(Collection.class.isAssignableFrom(cl)) {
					Type[] tar = pt.getActualTypeArguments();
					if(tar != null && tar.length == 1) { // Collection<T> required
						return (Class< ? >) tar[0];
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns T if instance.propertyname is a duplicate in some other instance in the list.
	 * @param <T>
	 * @param items
	 * @param instance
	 * @param propertyname
	 * @return
	 * @throws Exception
	 */
	static public <T> boolean hasDuplicates(List<T> items, T instance, String propertyname) throws Exception {
		ClassMetaModel cmm = findClassMeta(instance.getClass());
		PropertyMetaModel< ? > pmm = getPropertyMeta(instance.getClass(), propertyname);
		Object vi = pmm.getValue(instance);
		ClassMetaModel vcmm = vi == null ? null : findClassMeta(vi.getClass());
		for(T v : items) {
			if(areObjectsEqual(instance, v, cmm))
				continue;
			Object vl = pmm.getValue(v);
			if(areObjectsEqual(vi, vl, vcmm)) {
				return true;
			}
		}
		return false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generic data model utility functions.				*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param t
	 * @return
	 */
	static public String identify(Object t) {
		if(t == null)
			return "null";
		ClassMetaModel cmm = MetaManager.findClassMeta(t.getClass());
		if(cmm.isPersistentClass() && cmm.getPrimaryKey() != null) {
			try {
				Object k = cmm.getPrimaryKey().getValue(t);
				return t.getClass().getName() + "#" + k + " @" + System.identityHashCode(t);
			} catch(Exception x) {}
		}
		return t.toString() + " @" + System.identityHashCode(t);
	}

	/**
	 * Return the primary key field for a given instance. This throws IllegalArgumentException's when the
	 * instance passed is not persistent or has an unknown primary key. If the primary key is just null
	 * this returns null.
	 *
	 * @param instance
	 * @return
	 */
	static public Object getPrimaryKey(Object instance) throws Exception {
		return getPrimaryKey(instance, null);
	}

	static public Object getPrimaryKey(Object instance, ClassMetaModel cmm) throws Exception {
		if(instance == null)
			throw new IllegalArgumentException("Instance cannot be null");
		if(cmm == null)
			cmm = findClassMeta(instance.getClass());
		if(! cmm.isPersistentClass())
			throw new IllegalArgumentException("The instance " + identify(instance) + " is not a persistent class");
		PropertyMetaModel< ? > pmm = cmm.getPrimaryKey();
		if(pmm == null)
			throw new IllegalArgumentException("The instance " + identify(instance) + " has an undefined primary key (cannot be obtained by metadata)");
		return pmm.getValue(instance);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Expanding properties.								*/
	/*--------------------------------------------------------------*/

//	static {
//		SIMPLE = new HashSet<Class< ? >>();
//		SIMPLE.add(Integer.class);
//		SIMPLE.add(Integer.TYPE);
//		SIMPLE.add(Long.class);
//		SIMPLE.add(Long.TYPE);
//		SIMPLE.add(Character.class);
//		SIMPLE.add(Character.TYPE);
//		SIMPLE.add(Short.class);
//		SIMPLE.add(Short.TYPE);
//		SIMPLE.add(Byte.class);
//		SIMPLE.add(Byte.TYPE);
//		SIMPLE.add(Double.class);
//		SIMPLE.add(Double.TYPE);
//		SIMPLE.add(Float.class);
//		SIMPLE.add(Float.TYPE);
//		SIMPLE.add(Boolean.class);
//		SIMPLE.add(Boolean.TYPE);
//		SIMPLE.add(BigDecimal.class);
//		SIMPLE.add(String.class);
//		SIMPLE.add(BigInteger.class);
//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Generate metadata for search and display for testing*/
	/*--------------------------------------------------------------*/
	/**
	 * Try to calculate some search properties off a data class for debug/test pps, if enabled
	 * @param cm
	 * @return
	 */
	@Nonnull
	public static List<SearchPropertyMetaModel> calculateSearchProperties(ClassMetaModel cm) {
		if(!DeveloperOptions.getBool("domui.generatemeta", false))
			return Collections.emptyList();
		if(cm.getSearchProperties() != null && cm.getSearchProperties().size() > 0)
			return cm.getSearchProperties();

		//-- Make a selection of reasonable properties to search on. Skip any compounds.
		int order = 0;
		List<SearchPropertyMetaModel> res = new ArrayList<SearchPropertyMetaModel>();
		for(PropertyMetaModel< ? > pmm : cm.getProperties()) {
			if(DomUtil.isBasicType(pmm.getActualType())) {
				//-- Very basic. Only support small sizes;
				if(pmm.getLength() > 50)
					continue;

				//-- Accept
			} else if(pmm.getRelationType() == PropertyRelationType.UP) {
				//-- accept ;-)
			} else
				continue;

			//-- Accepted
			SearchPropertyMetaModelImpl sp = new SearchPropertyMetaModelImpl(cm);
			sp.setIgnoreCase(true);
			sp.setOrder(order++);
			sp.setPropertyName(pmm.getName());
			List<PropertyMetaModel< ? >> pl = new ArrayList<PropertyMetaModel< ? >>(1);
			pl.add(pmm);
			sp.setPropertyPath(pl);
			res.add(sp);
		}
		return res;
	}

	/**
	 * Generate some set of columns to show from a class' metadata, if enabled.
	 * @param cmm
	 * @return
	 */
	@Nonnull
	static public List<DisplayPropertyMetaModel> calculateObjectProperties(ClassMetaModel cm) {
		if(!DeveloperOptions.getBool("domui.generatemeta", false))
			return Collections.emptyList();

		List<DisplayPropertyMetaModel> res = new ArrayList<DisplayPropertyMetaModel>();
		int totlen = 0;
		for(PropertyMetaModel< ? > pmm : cm.getProperties()) {
			if(totlen > 512 || res.size() > 20)
				break;

			if(DomUtil.isBasicType(pmm.getActualType())) {
				//-- Very basic. Only support small sizes;
				if(pmm.getLength() > 50)
					continue;
				if(pmm.getLength() > 0)
					totlen += pmm.getLength();
				else if(pmm.getPrecision() > 0) {
					totlen += pmm.getPrecision();
				} else
					totlen += 10;

				//-- Accept
				//			} else if(pmm.getRelationType() == PropertyRelationType.UP) {
				//				//-- accept ;-)
			} else
				continue;

			DisplayPropertyMetaModel dp = new DisplayPropertyMetaModel();
			dp.setName(pmm.getName());
			res.add(dp);
		}

		return res;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:		*/
	/*--------------------------------------------------------------*/

	/**
	 * Get a NLSed label for the specified enum label.
	 */
	static public String getEnumLabel(Enum< ? > label) {
		if(label == null)
			return null;
		ClassMetaModel cmm = MetaManager.findClassMeta(label.getClass());
		String s = cmm.getDomainLabel(NlsContext.getLocale(), label);
		if(s == null)
			s = String.valueOf(label);
		return s;
	}

	/**
	 * Get a label for the enum value "value" presented on the property passed. This will first
	 * check to see if this property has overridden the labels for the enum before falling back
	 * to the enum's global bundle.
	 *
	 * @param clz
	 * @param property
	 * @param value
	 * @return
	 */
	static public String getEnumLabel(Class< ? > clz, String property, Object value) {
		if(value == null)
			return null;
		return getEnumLabel(MetaManager.findPropertyMeta(clz, property), value);
	}

	/**
	 * Get a label for the enum value "value" presented on the property passed. This will first
	 * check to see if this property has overridden the labels for the enum before falling back
	 * to the enum's global bundle.
	 * @param pmm
	 * @param value
	 * @return
	 */
	static public String getEnumLabel(PropertyMetaModel< ? > pmm, Object value) {
		if(value == null)
			return null;
		Locale loc = NlsContext.getLocale();
		String v = pmm.getDomainValueLabel(loc, value);
		if(v == null) {
			ClassMetaModel cmm = MetaManager.findClassMeta(pmm.getActualType());
			v = cmm.getDomainLabel(loc, value);
			if(v == null) {
				if(value.getClass() != cmm.getActualClass()) {
					cmm = MetaManager.findClassMeta(value.getClass());
					v = cmm.getDomainLabel(loc, value);
				}
				if(v == null)
					v = String.valueOf(value);
			}
		}
		return v;
	}

	/**
	 * Return the list of defined combo properties, either on property model or class model. Returns
	 * the empty list if none are defined.
	 * @param pmm
	 * @return
	 */
	@Nonnull
	static public List<DisplayPropertyMetaModel> getComboProperties(@Nonnull PropertyMetaModel< ? > pmm) {
		List<DisplayPropertyMetaModel> res = pmm.getComboDisplayProperties();
		if(res.size() != 0)
			return res;
		return pmm.getValueModel().getComboDisplayProperties();
	}

	/**
	 * Comparator to sort by ascending sortIndex.
	 */
	static public final Comparator<DisplayPropertyMetaModel> C_BY_SORT_INDEX = new Comparator<DisplayPropertyMetaModel>() {
		@Override
		public int compare(DisplayPropertyMetaModel a, DisplayPropertyMetaModel b) {
			return a.getSortIndex() - b.getSortIndex();
		}
	};


	/**
	 * Walk the list of properties, and defines the list that should be added as sort properties
	 * to the QCriteria.
	 * @param crit
	 * @param properties
	 */
	static public void applyPropertySort(@Nonnull QCriteria< ? > q, @Nonnull List<DisplayPropertyMetaModel> properties) {
		List<DisplayPropertyMetaModel> sl = new ArrayList<DisplayPropertyMetaModel>();
		boolean hasindex = false;
		for(DisplayPropertyMetaModel p : properties) {
			if(p.getSortable() == SortableType.SORTABLE_ASC || p.getSortable() == SortableType.SORTABLE_DESC)
				sl.add(p);
			if(p.getSortIndex() >= 0)
				hasindex = true;
		}
		if(sl.size() == 0)
			return;
		if(hasindex)
			Collections.sort(sl, C_BY_SORT_INDEX);
		for(DisplayPropertyMetaModel p : sl) {
			switch(p.getSortable()){
				default:
					throw new IllegalStateException("Unexpected sort type: " + p.getSortable());
				case SORTABLE_ASC:
					q.ascending(p.getName());
					break;
				case SORTABLE_DESC:
					q.descending(p.getName());
					break;
			}
		}
	}

	/**
	 * Fill target instance with same values as found in source instance. PK, TCN and transient properties would not be copied.
	 *
	 * @param <T>
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	static public <T> void fillCopy(@Nonnull T source, @Nonnull T target) throws Exception {
		fillCopy(source, target, false, false, false);
	}

	/**
	 * Fill target instance with same values as found in source instance. PK, TCN and transient properties would not be copied.
	 *
	 * @param <T>
	 * @param source
	 * @param target
	 * @param ignoredColumns Specified optional columns that would not be filled with data from source
	 * @throws Exception
	 */
	static public <T> void fillCopy(@Nonnull T source, @Nonnull T target, String... ignoredColumns) throws Exception {
		fillCopy(source, target, false, false, false, ignoredColumns);
	}

	/**
	 * Fill target instance with same values as found in source instance.
	 *
	 * @param <T>
	 * @param source
	 * @param target
	 * @param copyPK If T, it also copies PK value(s)
	 * @param copyTCN If T, it also copies TCN value(s)
	 * @param copyTransient If T, it also copies transient values
	 * @param ignoredColumns Specified optional columns that would not be filled with data from source
	 * @throws Exception
	 */
	static public <T> void fillCopy(@Nonnull T source, @Nonnull T target, boolean copyPK, boolean copyTCN, boolean copyTransient, String... ignoredColumns) throws Exception {
		ClassMetaModel cmm = MetaManager.findClassMeta(source.getClass());
		List<String> ignoreList = new ArrayList<String>(ignoredColumns.length);
		for (String ignore : ignoredColumns) {
			ignoreList.add(ignore);
		}
		for (PropertyMetaModel< ? > pmm : cmm.getProperties()) {
			PropertyMetaModel< Object > opmm = (PropertyMetaModel< Object >) pmm;
			if((!opmm.isPrimaryKey() || copyPK) && //
				(!opmm.isTransient() || copyTransient) && //
				(!"tcn".equalsIgnoreCase(opmm.getName()) || copyTCN) && //
				(ignoreList.size() == 0 || !ignoreList.contains(opmm.getName()))) {
				opmm.setValue(target, opmm.getValue(source));
			}
		}
	}


}
