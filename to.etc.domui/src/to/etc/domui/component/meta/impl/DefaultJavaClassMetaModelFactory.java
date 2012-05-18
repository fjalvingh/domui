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
package to.etc.domui.component.meta.impl;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * A default, base implementation of a MetaModel layer. This tries to discover metadata by using
 * base property information plus Hibernate/JPA annotation data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public class DefaultJavaClassMetaModelFactory implements IClassMetaModelFactory {

	@Override
	public int accepts(@Nonnull Object theThingy) {
		if(!(theThingy instanceof Class< ? >)) // Only accept Class<?> thingies.
			return -1;
		return 1;
	}

	/**
	 *
	 * @see to.etc.domui.component.meta.IClassMetaModelFactory#createModel(java.lang.Object)
	 */
	@Override
	@Nonnull
	public ClassMetaModel createModel(List<Runnable> init, @Nonnull Object theThingy) {
		Class< ? > clz = (Class< ? >) theThingy;
		final DefaultJavaClassInfo colli = new DefaultJavaClassInfo(new DefaultClassMetaModel(clz), init);

		/*
		 * Business as usual: the Introspector does not properly resolve properties when using
		 * invariant returns. We're forced to do something by ourselves. The Introspector does
		 * not return the defined method in the class, but it returns the synthetic proxy generated
		 * by the compiler with the fixed "Object" return type. This means that the return type
		 * would be incorrect, but even worse: the generated method lacks the annotations on
		 * the real method. This caused metadata to be unavailable for classes that implemented
		 * IIdentifyable&gt;Long&gl;.
		 *
		 * BeanInfo bi = Introspector.getBeanInfo(m_metaClass);
		 * PropertyDescriptor[] ar = bi.getPropertyDescriptors();
		 */
		List<PropertyInfo> pilist = ClassUtil.getProperties(colli.getTypeClass());
		createPropertyMetas(colli, pilist);

		decodeProperties(colli); // Phase 1: decode primitive properties.
		decodeDomainValues(colli); // Handle domain for this class (list-of-values)
		decodeClassAnnotations(colli); // Do class-level annotations

		colli.later(new Runnable() {
			@Override
			public void run() {
				//-- Set both search property lists ordered by their order property.
				Collections.sort(colli.getSearchList(), SearchPropertyMetaModel.BY_ORDER);
				Collections.sort(colli.getKeySearchList(), SearchPropertyMetaModel.BY_ORDER);
				colli.getModel().setSearchProperties(colli.getSearchList());
				colli.getModel().setKeyWordSearchProperties(colli.getKeySearchList());
			}
		});

		return colli.getModel();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Phase one: flat properties decode in this class.	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create {@link PropertyMetaModel} instances for all gettable properties. These are only
	 * initialized with their primary property info.
	 * @param colli
	 * @param pilist
	 */
	private void createPropertyMetas(DefaultJavaClassInfo colli, List<PropertyInfo> pilist) {
		List<PropertyMetaModel< ? >> reslist = new ArrayList<PropertyMetaModel< ? >>(pilist.size());

		//-- Create model data from this thingy.
		for(PropertyInfo pd : pilist) {
			if(!pd.getName().equals("class")) {
				Method rm = pd.getGetter();
				if(rm.getParameterTypes().length != 0)
					continue;
				DefaultPropertyMetaModel< ? > pm = new DefaultPropertyMetaModel<Object>(colli.getModel(), pd);
				reslist.add(pm);
				colli.getMap().put(pd, pm);
			}
		}
		colli.getModel().setClassProperties(reslist);
	}

	/**
	 * This obtains all properties from the class and initializes their models.
	 * @param dmm
	 * @param m_clz
	 * @param colli
	 * @param m_keySearchList
	 * @param m_searchList
	 */
	protected void decodeProperties(final DefaultJavaClassInfo colli) {
		for(Map.Entry<PropertyInfo, DefaultPropertyMetaModel< ? >> e : colli.getMap().entrySet()) {
			updatePropertyInfo(colli, e.getValue(), e.getKey());
		}
	}

	protected void updatePropertyInfo(final DefaultJavaClassInfo colli, DefaultPropertyMetaModel< ? > pm, PropertyInfo pd) {
		initPropertyModel(colli, pd, pm);
		if(pm.isPrimaryKey())
			colli.getModel().setPrimaryKey(pm);
	}

	protected void initPropertyModel(DefaultJavaClassInfo colli, PropertyInfo pd, DefaultPropertyMetaModel< ? > pmm) {
		Annotation[] annar = pd.getGetter().getAnnotations();
		for(Annotation an : annar) {
			String ana = an.annotationType().getName();
			decodePropertyAnnotationByName(colli, pmm, an, ana);
			decodePropertyAnnotation(colli, pmm, an);
		}
	}

	@SuppressWarnings({"cast", "unchecked", "rawtypes"})
	protected <T> void decodePropertyAnnotation(final DefaultJavaClassInfo colli, final DefaultPropertyMetaModel<T> pmm, Annotation an) {
		final DefaultClassMetaModel cmm = colli.getModel();
		if(an instanceof MetaProperty) {
			//-- Handle meta-assignments.
			MetaProperty mp = (MetaProperty) an;
			if(mp.defaultSortable() != SortableType.UNKNOWN)
				pmm.setSortable(mp.defaultSortable());
			if(mp.length() >= 0)
				pmm.setLength(mp.length());
			if(mp.displaySize() >= 0)
				pmm.setDisplayLength(mp.displaySize());
			if(mp.noWrap() != YesNoType.UNKNOWN)
				pmm.setNowrap(mp.noWrap());
			if(mp.required() != YesNoType.UNKNOWN)
				pmm.setRequired(mp.required() == YesNoType.YES);
			if(mp.converterClass() != DummyConverter.class)
				pmm.setConverter((IConverter<T>) ConverterRegistry.getConverterInstance((Class< ? extends IConverter<T>>) mp.converterClass()));
			if(mp.editpermissions().length != 0)
				pmm.setEditRoles(makeRoleSet(mp.editpermissions()));
			if(mp.viewpermissions().length != 0)
				pmm.setViewRoles(makeRoleSet(mp.viewpermissions()));
			if(mp.temporal() != TemporalPresentationType.UNKNOWN && pmm.getTemporal() == TemporalPresentationType.UNKNOWN)
				pmm.setTemporal(mp.temporal());
			if(mp.numericPresentation() != NumericPresentation.UNKNOWN)
				pmm.setNumericPresentation(mp.numericPresentation());
			if(pmm.getReadOnly() != YesNoType.YES) // Do not override readonlyness from missing write method
				pmm.setReadOnly(mp.readOnly());
			if(mp.componentTypeHint().length() != 0)
				pmm.setComponentTypeHint(mp.componentTypeHint());

			//-- Convert validators.
			List<MetaPropertyValidatorImpl> list = new ArrayList<MetaPropertyValidatorImpl>();
			for(Class< ? extends IValueValidator< ? >> vv : mp.validator()) {
				MetaPropertyValidatorImpl vi = new MetaPropertyValidatorImpl(vv);
				list.add(vi);
			}
			for(MetaValueValidator mvv : mp.parameterizedValidator()) {
				MetaPropertyValidatorImpl vi = new MetaPropertyValidatorImpl(mvv.validator(), mvv.parameters());
				list.add(vi);
			}
			pmm.setValidators(list.toArray(new PropertyMetaValidator[list.size()]));

			//-- Regexp validators.
			if(mp.regexpValidation().length() > 0) {
				try {
					//-- Precompile to make sure it's valid;
					Pattern.compile(mp.regexpValidation());
				} catch(Exception x) {
					throw new MetaModelException(Msgs.BUNDLE, Msgs.MM_BAD_REGEXP, mp.regexpValidation(), this.toString());
				}
				pmm.setRegexpValidator(mp.regexpValidation());
				if(mp.regexpUserString().length() > 0)
					pmm.setRegexpUserString(mp.regexpUserString());
			}
		} else if(an instanceof MetaCombo) {
			final MetaCombo c = (MetaCombo) an;
			if(c.dataSet() != UndefinedComboDataSet.class) {
				pmm.setRelationType(PropertyRelationType.UP);
				pmm.setComboDataSet(c.dataSet());
			}
			if(c.labelRenderer() != UndefinedLabelStringRenderer.class) {
				pmm.setRelationType(PropertyRelationType.UP);
				pmm.setComboLabelRenderer(c.labelRenderer());
			}
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class) {
				pmm.setRelationType(PropertyRelationType.UP);
				pmm.setComboNodeRenderer(c.nodeRenderer());
			}
			pmm.setComponentTypeHint(Constants.COMPONENT_COMBO);
			if(c.properties() != null && c.properties().length > 0) {
				pmm.setRelationType(PropertyRelationType.UP);
				colli.later(new Runnable() {
					@Override
					public void run() {
						pmm.setComboDisplayProperties(DisplayPropertyMetaModel.decode(pmm.getValueModel(), c.properties()));
					}
				});
			}
		} else if(an instanceof MetaSearch) {
			MetaSearch sp = (MetaSearch) an;
			List<PropertyMetaModel< ? >> ppl = new ArrayList<PropertyMetaModel< ? >>(1);
			ppl.add(pmm);
			SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(colli.getModel(), ppl);
			mm.setIgnoreCase(sp.ignoreCase());
			mm.setOrder(sp.order());
			mm.setMinLength(sp.minLength());
			mm.setPropertyName(pmm.getName());
			if(sp.searchType() == SearchPropertyType.SEARCH_FIELD || sp.searchType() == SearchPropertyType.BOTH) {
				colli.getSearchList().add(mm);
			}
			if(sp.searchType() == SearchPropertyType.KEYWORD || sp.searchType() == SearchPropertyType.BOTH) {
				colli.getKeySearchList().add(mm);
			}
		} else if(an instanceof MetaObject) {
			/*
			 * Table metamodel.
			 */
			final MetaObject o = (MetaObject) an;
			if(o.selectedRenderer() != UndefinedLabelStringRenderer.class)
				pmm.setLookupSelectedRenderer(o.selectedRenderer());

			colli.later(new Runnable() {
				@Override
				public void run() {
					if(o.selectedProperties().length != 0) {
						pmm.setLookupSelectedProperties(DisplayPropertyMetaModel.decode(cmm, o.selectedProperties()));
					}

					if(o.defaultColumns().length > 0) {
						pmm.setLookupTableProperties(DisplayPropertyMetaModel.decode(cmm, o.defaultColumns()));
					}
				}
			});

			if(o.defaultSortColumn() != Constants.NONE) {}
			if(o.defaultSortOrder() != SortableType.UNKNOWN) {
				// FIXME Missing metadata!!

			}

			if(o.searchProperties().length > 0) {
				int index = 0;
				List<SearchPropertyMetaModel> propsearchlist = new ArrayList<SearchPropertyMetaModel>();
				List<SearchPropertyMetaModel> propkeysearchlist = new ArrayList<SearchPropertyMetaModel>();

				for(MetaSearchItem msi : o.searchProperties()) {
					index++;
					List<PropertyMetaModel< ? >> ppl = MetaManager.parsePropertyPath(colli.getModel(), msi.name());
					SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(cmm, ppl);
					mm.setIgnoreCase(msi.ignoreCase());
					mm.setOrder(msi.order() == -1 ? index : msi.order());
					mm.setMinLength(msi.minLength());
					mm.setPropertyName(msi.name().length() == 0 ? null : msi.name());
					mm.setLookupLabelKey(msi.lookupLabelKey().length() == 0 ? null : msi.lookupLabelKey());
					mm.setLookupHintKey(msi.lookupHintKey().length() == 0 ? null : msi.lookupHintKey());
					if(msi.searchType() == SearchPropertyType.SEARCH_FIELD || msi.searchType() == SearchPropertyType.BOTH) {
						propsearchlist.add(mm);
					}
					if(msi.searchType() == SearchPropertyType.KEYWORD || msi.searchType() == SearchPropertyType.BOTH) {
						propkeysearchlist.add(mm);
					}
				}
				pmm.setLookupFieldKeySearchProperties(propkeysearchlist);
				pmm.setLookupFieldSearchProperties(propsearchlist);
			}
		}
	}

	protected void decodePropertyAnnotationByName(DefaultJavaClassInfo colli, DefaultPropertyMetaModel< ? > pmm, Annotation an, String name) {
		DefaultClassMetaModel cmm = colli.getModel();
		if("javax.persistence.Column".equals(name)) {
			decodeJpaColumn(pmm, an);
		} else if("javax.persistence.Id".equals(name)) {
			pmm.setPrimaryKey(true);
			cmm.setPersistentClass(true);
		} else if("javax.persistence.ManyToOne".equals(name) || "javax.persistence.OneToOne".equals(name)) {
			pmm.setRelationType(PropertyRelationType.UP);

			//-- Decode fields from the annotation.
			try {
				Boolean op = (Boolean) DomUtil.getClassValue(an, "optional");
				pmm.setRequired(!op.booleanValue());
			} catch(Exception x) {
				Trouble.wrapException(x);
			}
		} else if("javax.persistence.Temporal".equals(name)) {
			try {
				Object val = DomUtil.getClassValue(an, "value");
				if(val != null) {
					String s = val.toString();
					if("DATE".equals(s))
						pmm.setTemporal(TemporalPresentationType.DATE);
					else if("TIME".equals(s))
						pmm.setTemporal(TemporalPresentationType.TIME);
					else if("TIMESTAMP".equals(s))
						pmm.setTemporal(TemporalPresentationType.DATETIME);
				}
			} catch(Exception x) {
				Trouble.wrapException(x);
			}
		} else if("javax.persistence.Transient".equals(name) || "org.hibernate.annotations.Formula".equals(name)) {
			pmm.setTransient(true);
		} else if("javax.persistence.OneToMany".equals(name)) {
			//-- This must be a list
			if(!Collection.class.isAssignableFrom(pmm.getActualType()))
				throw new IllegalStateException("Invalid property type for DOWN relation of property " + this + ": only List<T> is allowed");
			pmm.setRelationType(PropertyRelationType.DOWN);
		} else if("to.etc.webapp.qsql.QJdbcId".equals(name)) {
			pmm.setPrimaryKey(true);
			cmm.setPersistentClass(true);
		}
	}

	/**
	 * Generically decode a JPA javax.persistence.Column annotation.
	 * FIXME Currently only single-column properties are supported.
	 * @param pmm
	 * @param an
	 */
	protected void decodeJpaColumn(DefaultPropertyMetaModel< ? > pmm, final Annotation an) {
		try {
			/*
			 * Handle the "length" annotation. As usual, someone with a brain the size of a pea fucked up the standard. The
			 * default value for the length is 255, which is of course a totally reasonable size. This makes it impossible to
			 * see if someone has actually provided a value. This means that in absence of the length the field is fucking
			 * suddenly 255 characters big. To prevent this utter disaster from fucking up the data we only accept this for
			 * STRING fields.
			 */
			Integer iv = (Integer) DomUtil.getClassValue(an, "length");
			pmm.setLength(iv.intValue());
			if(pmm.getLength() == 255) { // Idiot value?
				if(pmm.getActualType() != String.class)
					pmm.setLength(-1);
			}

			Boolean bv = (Boolean) DomUtil.getClassValue(an, "nullable");
			pmm.setRequired(!bv.booleanValue());
			iv = (Integer) DomUtil.getClassValue(an, "precision");
			pmm.setPrecision(iv.intValue());
			iv = (Integer) DomUtil.getClassValue(an, "scale");
			pmm.setScale(iv.intValue());
			String name = (String) DomUtil.getClassValue(an, "name");
			if(null == name) {
				name = pmm.getName(); // If column is present but name is null- use the property name verbatim.
			}
			pmm.setColumnNames(new String[]{name});
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new WrappedException(x);
		}
	}

	/**
	 * Decode stringset containing xxx+xxx to duparray.
	 * @param s
	 * @return
	 */
	static private String[][] makeRoleSet(final String[] sar) {
		if(sar.length == 0)
			return null;
		String[][] mapset = new String[sar.length][];
		ArrayList<String> al = new ArrayList<String>(10);
		int ix = 0;
		for(String s : sar) {
			StringTokenizer st = new StringTokenizer(s, ";+ \t,");
			al.clear();
			while(st.hasMoreElements()) {
				al.add(st.nextToken());
			}
			mapset[ix] = al.toArray(new String[al.size()]);
		}
		return mapset;
	}

	/**
	 * If this is an enum or the class Boolean define it's domain values.
	 * @param init
	 * @param dmm
	 * @param m_clz
	 */
	protected void decodeDomainValues(DefaultJavaClassInfo colli) {
		//-- If this is an enumerable thingerydoo...
		if(colli.getTypeClass() == Boolean.class) {
			colli.getModel().setDomainValues(new Object[]{Boolean.FALSE, Boolean.TRUE});
		} else if(Enum.class.isAssignableFrom(colli.getTypeClass())) {
			Class<Enum< ? >> ecl = (Class<Enum< ? >>) colli.getTypeClass();
			colli.getModel().setDomainValues(ecl.getEnumConstants());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Annotation handling.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Walk all known class annotations and use them to add class based metadata.
	 * @param i
	 * @param m_keySearchList
	 * @param m_searchList
	 */
	protected void decodeClassAnnotations(DefaultJavaClassInfo colli) {
		Annotation[] annar = colli.getTypeClass().getAnnotations(); // All class-level thingerydoos
		for(Annotation an : annar) {
			String ana = an.annotationType().getName(); // Get the annotation's name
			decodeClassAnnotationByName(colli, an, ana); // Decode by name literal
			decodeClassAnnotation(colli, an); // Decode well-known annotations
		}
	}

	/**
	 * Can be overridden to decode user-specific annotations. Currently only decodes the javax.persistence.Table annotation.
	 * @param pha
	 * @param an
	 * @param name
	 */
	protected void decodeClassAnnotationByName(@Nonnull final DefaultJavaClassInfo colli, @Nonnull final Annotation an, @Nonnull final String name) {
		if("javax.persistence.Table".equals(name)) {
			//-- Decode fields from the annotation.
			try {
				String tablename = (String) DomUtil.getClassValue(an, "name");
				String tableschema = (String) DomUtil.getClassValue(an, "schema");
				if(tablename != null) {
					if(tableschema != null)
						tablename = tableschema + "." + tablename;
					colli.getModel().setTableName(tablename);
				}
			} catch(Exception x) {
				Trouble.wrapException(x);
			}
		} else if("to.etc.webapp.qsql.QJdbcTable".equals(name)) {
			colli.getModel().setPersistentClass(true);
		}
	}

	/**
	 * Decodes all DomUI annotations.
	 * @param pha
	 * @param an
	 * @param m_keySearchList
	 * @param m_searchList
	 */
	protected void decodeClassAnnotation(final DefaultJavaClassInfo colli, final Annotation an) {
		final DefaultClassMetaModel cmm = colli.getModel();
		if(an instanceof MetaCombo) {
			final MetaCombo c = (MetaCombo) an;
			if(c.dataSet() != UndefinedComboDataSet.class)
				cmm.setComboDataSet(c.dataSet());
			if(c.labelRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setComboLabelRenderer(c.labelRenderer());
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setComboNodeRenderer(c.nodeRenderer());
			if(c.properties() != null && c.properties().length > 0) {
				colli.later(new Runnable() {
					@Override
					public void run() {
						cmm.setComboDisplayProperties(DisplayPropertyMetaModel.decode(cmm, c.properties()));
					}
				});

			}
			if(c.preferred())
				cmm.setComponentTypeHint(Constants.COMPONENT_COMBO);
		} else if(an instanceof MetaObject) {
			final MetaObject mo = (MetaObject) an;

			if(mo.defaultColumns().length > 0) {
				colli.later(new Runnable() {
					@Override
					public void run() {
						cmm.setTableDisplayProperties(DisplayPropertyMetaModel.decode(cmm, mo.defaultColumns()));
					}
				});
			}
			if(!mo.defaultSortColumn().equals(Constants.NONE))
				cmm.setDefaultSortProperty(mo.defaultSortColumn());
			cmm.setDefaultSortDirection(mo.defaultSortOrder());

			if(mo.selectedRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setLookupSelectedRenderer(mo.selectedRenderer());
			if(mo.selectedProperties().length != 0) {
				colli.later(new Runnable() {
					@Override
					public void run() {
						cmm.setLookupSelectedProperties(DisplayPropertyMetaModel.decode(cmm, mo.selectedProperties()));
					}
				});
			}

			//-- Handle search
			int index = 0;
			for(MetaSearchItem msi : mo.searchProperties()) {
				index++;
				List<PropertyMetaModel< ? >> pl = MetaManager.parsePropertyPath(colli.getModel(), msi.name());
				SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(cmm, pl);
				mm.setIgnoreCase(msi.ignoreCase());
				mm.setOrder(msi.order() == -1 ? index : msi.order());
				mm.setMinLength(msi.minLength());
				mm.setPropertyName(msi.name());
				mm.setLookupLabelKey(msi.lookupLabelKey().length() == 0 ? null : msi.lookupLabelKey());
				mm.setLookupHintKey(msi.lookupHintKey().length() == 0 ? null : msi.lookupHintKey());
				if(msi.searchType() == SearchPropertyType.SEARCH_FIELD || msi.searchType() == SearchPropertyType.BOTH) {
					colli.getSearchList().add(mm);
				}
				if(msi.searchType() == SearchPropertyType.KEYWORD || msi.searchType() == SearchPropertyType.BOTH) {
					colli.getKeySearchList().add(mm);
				}
			}
		}
	}
}
