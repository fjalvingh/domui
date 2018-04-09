package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;
import to.etc.domui.component.meta.impl.DefaultPropertyMetaModel;
import to.etc.domui.trouble.Trouble;
import to.etc.domui.util.DomUtil;
import to.etc.util.WrappedException;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Decode default simple annotations on the property, like jpa/Hibernate.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-10-17.
 */
@DefaultNonNull
public class MIBasicPropertyAnnotations implements IPropertyMetaProvider<DefaultClassMetaModel, DefaultPropertyMetaModel<?>> {
	@Override public void provide(@Nonnull MetaInitContext context, @Nonnull DefaultClassMetaModel cmm, @Nonnull DefaultPropertyMetaModel<?> pmm) throws Exception {
		Annotation[] annar = pmm.getDescriptor().getGetter().getAnnotations();
		for(Annotation an : annar) {
			String ana = an.annotationType().getName();
			decodePropertyAnnotationByName(cmm, pmm, an, ana);
			//decodePropertyAnnotation(colli, pmm, an);
		}
		if(pmm.isPrimaryKey())
			cmm.setPrimaryKey(pmm);
	}

	protected void decodePropertyAnnotationByName(DefaultClassMetaModel cmm, DefaultPropertyMetaModel< ? > pmm, Annotation an, String name) {
		if("javax.persistence.Column".equals(name)) {
			decodeJpaColumn(pmm, an);
		} else if("javax.persistence.JoinColumn".equals(name)) {
			decodeJpaJoinColumn(pmm, an);
		} else if("javax.persistence.Id".equals(name) || "javax.persistence.EmbeddedId".equals(name)) {
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
	protected void decodeJpaColumn(@Nonnull DefaultPropertyMetaModel< ? > pmm, @Nonnull final Annotation an) {
		try {
			/*
			 * Handle the "length" annotation. As usual, someone with a brain the size of a pea f.cked up the standard. The
			 * default value for the length is 255, which is of course a totally reasonable size. This makes it impossible to
			 * see if someone has actually provided a value. This means that in absence of the length the field is fscking
			 * suddenly 255 characters big. To prevent this utter disaster from f'ing up the data we only accept this for
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
	 * Generically decode a JPA  javax.persistence.JoinColumn annotation.
	 * @param pmm
	 * @param an
	 */
	protected void decodeJpaJoinColumn(@Nonnull DefaultPropertyMetaModel< ? > pmm, @Nonnull final Annotation an) {
		try {
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
}
