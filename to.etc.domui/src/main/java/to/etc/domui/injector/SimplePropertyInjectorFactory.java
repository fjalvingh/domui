package to.etc.domui.injector;

import to.etc.domui.annotations.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.*;

import javax.annotation.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

/**
 * Accepts properties that are annotated with URLParameter, and that are simple
 * value properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
@DefaultNonNull
final public class SimplePropertyInjectorFactory implements IPagePropertyFactory {
	final private Set<String> m_ucs = new HashSet<String>();

	public SimplePropertyInjectorFactory() {
		m_ucs.add(String.class.getName());
		m_ucs.add(Byte.class.getName());
		m_ucs.add(Byte.TYPE.getName());
		m_ucs.add(Character.class.getName());
		m_ucs.add(Character.TYPE.getName());
		m_ucs.add(Short.class.getName());
		m_ucs.add(Short.TYPE.getName());
		m_ucs.add(Integer.class.getName());
		m_ucs.add(Integer.TYPE.getName());
		m_ucs.add(Long.class.getName());
		m_ucs.add(Long.TYPE.getName());
		m_ucs.add(Float.class.getName());
		m_ucs.add(Float.TYPE.getName());
		m_ucs.add(Double.class.getName());
		m_ucs.add(Double.TYPE.getName());
		m_ucs.add(Date.class.getName());
		m_ucs.add(BigDecimal.class.getName());
		m_ucs.add(BigInteger.class.getName());
		m_ucs.add(Boolean.class.getName());
		m_ucs.add(Boolean.TYPE.getName());
	}

	@Nullable @Override public PropertyInjector calculateInjector(PropertyInfo propertyInfo) {
		Method getter = propertyInfo.getGetter();
		UIUrlParameter upp = null;
		if(null != getter) {
			upp = ClassUtil.findAnnotationIncludingSuperClasses(getter, UIUrlParameter.class);
		}
		Method setter = propertyInfo.getSetter();
		if(null != setter && upp == null) {
			upp = ClassUtil.findAnnotationIncludingSuperClasses(setter, UIUrlParameter.class);
		}
		if(null == upp)
			return null;

		if(null == setter)
			throw new ProgrammerErrorException(UIUrlParameter.class.getSimpleName() + " annotation cannot be used on a setterless property (is the setter private?)");

		String name = upp.name() == Constants.NONE ? propertyInfo.getName() : upp.name();
		Class< ? > ent = upp.entity();
		if(ent == Object.class) {
			//-- Use getter's type.
			ent = propertyInfo.getActualType();
		}

		/*
		 * if entity is specified we're always certain we have a non primitive.
		 */
		if(upp.entity() != Object.class)
			return null;

		//-- Can be entity or literal.
		if(upp.name() == Constants.NONE ||					// If no name is set this is NEVER an entity,
			m_ucs.contains(ent.getName()) ||
			RuntimeConversions.isSimpleType(propertyInfo.getActualType()) ||
			RuntimeConversions.isEnumType(propertyInfo.getActualType())) {
			return createParameterInjector(propertyInfo, name, upp.mandatory());
		}
		return null;
	}

	protected PropertyInjector createParameterInjector(PropertyInfo pi, String name, boolean mandatory) {
		return new UrlParameterInjector(pi.getSetter(), name, mandatory);
	}
}
