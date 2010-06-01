package to.etc.domui.component.meta.impl;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
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
	public ClassMetaModel createModel(@Nonnull Object theThingy) {
		Class< ? > clz = (Class< ? >) theThingy;
		DefaultClassMetaModel dmm = new DefaultClassMetaModel(clz);

		decodeClassAnnotations(dmm, clz); // Do class-level annotations
		decodeDomainValues(dmm, clz); // Handle domain for this class (list-of-values)
		decodeProperties(dmm, clz);

		return dmm;
	}

	/**
	 * This obtains all properties from the class and initializes their models.
	 * @param dmm
	 * @param clz
	 */
	protected void decodeProperties(DefaultClassMetaModel cmm, Class< ? > clz) {
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
		List<PropertyInfo> pilist = ClassUtil.getProperties(clz);

		//-- Create model data from this thingy.
		for(PropertyInfo pd : pilist) {
			if(!pd.getName().equals("class"))
				createPropertyInfo(cmm, pd);
		}
	}

	protected void createPropertyInfo(DefaultClassMetaModel cmm, final PropertyInfo pd) {
		//		System.out.println("Property: " + pd.getName() + ", reader=" + pd.getGetter());
		//		if(pd.getName().equals("id"))
		//			System.out.println("GOTCHA");

		Method rm = pd.getGetter();
		if(rm.getParameterTypes().length != 0)
			return;
		DefaultPropertyMetaModel pm = new DefaultPropertyMetaModel(cmm, pd);
		cmm.addProperty(pm);
		if(pm.isPrimaryKey())
			cmm.setPrimaryKey(pm);
	}

	/**
	 * If this is an enum or the class Boolean define it's domain values.
	 * @param dmm
	 * @param clz
	 */
	protected void decodeDomainValues(DefaultClassMetaModel dmm, Class< ? > clz) {
		//-- If this is an enumerable thingerydoo...
		if(clz == Boolean.class) {
			dmm.setDomainValues(new Object[]{Boolean.FALSE, Boolean.TRUE});
		} else if(Enum.class.isAssignableFrom(clz)) {
			Class<Enum< ? >> ecl = (Class<Enum< ? >>) clz;
			dmm.setDomainValues(ecl.getEnumConstants());
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Annotation handling.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Walk all known class annotations and use them to add class based metadata.
	 */
	protected void decodeClassAnnotations(DefaultClassMetaModel cmm, Class< ? > clz) {
		Annotation[] annar = clz.getAnnotations(); // All class-level thingerydoos
		for(Annotation an : annar) {
			String ana = an.annotationType().getName(); // Get the annotation's name
			decodeAnnotationByName(cmm, an, ana); // Decode by name literal
			decodeAnnotation(cmm, an); // Decode well-known annotations
		}
	}

	/**
	 * Can be overridden to decode user-specific annotations. Currently only decodes the javax.persistence.Table annotation.
	 * @param an
	 * @param name
	 */
	protected void decodeAnnotationByName(@Nonnull final DefaultClassMetaModel cmm, @Nonnull final Annotation an, @Nonnull final String name) {
		if("javax.persistence.Table".equals(name)) {
			//-- Decode fields from the annotation.
			try {
				String tablename = (String) DomUtil.getClassValue(an, "name");
				String tableschema = (String) DomUtil.getClassValue(an, "schema");
				if(tablename != null) {
					if(tableschema != null)
						tablename = tableschema + "." + tablename;
					cmm.setTableName(tablename);
				}
			} catch(Exception x) {
				Trouble.wrapException(x);
			}
		}
	}

	/**
	 * Decodes all DomUI annotations.
	 * @param an
	 */
	protected void decodeAnnotation(final DefaultClassMetaModel cmm, final Annotation an) {
		if(an instanceof MetaCombo) {
			MetaCombo c = (MetaCombo) an;
			if(c.dataSet() != UndefinedComboDataSet.class)
				cmm.setComboDataSet(c.dataSet());
			if(c.labelRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setComboLabelRenderer(c.labelRenderer());
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setComboNodeRenderer(c.nodeRenderer());
			if(c.optional() != ComboOptionalType.INHERITED)
				cmm.setComboOptional(c.optional());
			if(c.properties() != null && c.properties().length > 0) {
				cmm.setComboDisplayProperties(DisplayPropertyMetaModel.decode(cmm, c.properties()));
			}
			cmm.setComponentTypeHint(Constants.COMPONENT_COMBO);
		} else if(an instanceof MetaLookup) {
			MetaLookup c = (MetaLookup) an;
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				cmm.setLookupFieldRenderer(c.nodeRenderer());
			if(c.properties().length != 0)
				cmm.setLookupFieldDisplayProperties(DisplayPropertyMetaModel.decode(cmm, c.properties()));
			cmm.setComponentTypeHint(Constants.COMPONENT_LOOKUP);
		} else if(an instanceof MetaObject) {
			MetaObject mo = (MetaObject) an;
			if(mo.defaultColumns().length > 0) {
				cmm.setTableDisplayProperties(DisplayPropertyMetaModel.decode(cmm, mo.defaultColumns()));
			}
			if(!mo.defaultSortColumn().equals(Constants.NONE))
				cmm.setDefaultSortProperty(mo.defaultSortColumn());
			cmm.setDefaultSortDirection(mo.defaultSortOrder());
		} else if(an instanceof MetaSearch) {
			MetaSearch ms = (MetaSearch) an;
			int index = 0;
			for(MetaSearchItem msi : ms.value()) {
				index++;
				SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl(cmm);
				mm.setIgnoreCase(msi.ignoreCase());
				mm.setOrder(msi.order() == -1 ? index : msi.order());
				mm.setMinLength(msi.minLength());
				mm.setPropertyName(msi.name().length() == 0 ? null : msi.name());
				mm.setLookupLabelKey(msi.lookupLabelKey().length() == 0 ? null : msi.lookupLabelKey());
				mm.setLookupHintKey(msi.lookupHintKey().length() == 0 ? null : msi.lookupHintKey());

				//FIXME NEED TO ADD HERE ACCORDING TO TYPE.
				//				if(msi. == SearchPropertyType.SEARCH_FIELD) {
				//					((DefaultClassMetaModel) getClassModel()).addSearchProperty(mm);
				//				}
				//				if(searchType == SearchPropertyType.KEYWORD) {
				//					((DefaultClassMetaModel) getClassModel()).addKeyWordSearchProperty(mm);
				//				}
				cmm.addSearchProperty(mm);
			}
		}
	}

}
