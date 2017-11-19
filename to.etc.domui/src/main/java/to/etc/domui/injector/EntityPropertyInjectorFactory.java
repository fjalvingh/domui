package to.etc.domui.injector;

import to.etc.domui.annotations.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.qsql.*;

import javax.annotation.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * If a property has an UIUrlParameter annotation and it's type refers to some JPA/Hibernate/QJdbc entity
 * then this will create an injector that injects the looked up entity by it's PK.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
@DefaultNonNull
final public class EntityPropertyInjectorFactory implements IPagePropertyFactory {
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

		if(! isValidEntity(ent))
			return null;

		//-- Try to find the PK for this entity
		ClassMetaModel cmm = MetaManager.findClassMeta(ent);
		PropertyMetaModel< ? > pmm = cmm.getPrimaryKey(); 					// Find it's PK;
		if(pmm == null)
			return null;

		return new UrlFindEntityByPkInjector(propertyInfo.getSetter(), name, upp.mandatory(), ent, pmm);
	}

	private boolean isValidEntity(Class<?> clz) {
		if(clz.getAnnotation(QJdbcTable.class) != null)
			return true;

		for(Annotation annotation : clz.getAnnotations()) {
			if(isValidEntity(annotation.annotationType().getName())) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidEntity(String name) {
		if("javax.persistence.Entity".equals(name))
			return true;
		return "javax.persistence.Table".equals(name);
	}
}
