package to.etc.domui.subinjector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.annotations.UIReinject;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.WikiUrls;
import to.etc.util.ClassUtil;
import to.etc.webapp.ProgrammerErrorException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * This factory scans for fields in the class and adds injectors for those.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-12-18.
 */
@NonNullByDefault
final public class SubPageFieldInjectorFactory implements ISubPageInjectorFactory{
	@Override
	public List<ISubPageInjector> calculateInjectors(Class<? extends SubPage> pageClass) {
		List<ISubPageInjector> list = new ArrayList<>();
		List<Field> fieldList = ClassUtil.getAllFields(pageClass);
		for(Field field : fieldList) {
			checkInjector(list, pageClass, field);
		}
		return list;
	}

	private void checkInjector(List<ISubPageInjector> list, Class<? extends SubPage> pageClass, Field field) {
		UIReinject ann = field.getAnnotation(UIReinject.class);
		boolean isFinal = Modifier.isFinal(field.getModifiers());
		Class<?> fieldType = calculateFieldType(field);

		ClassMetaModel classMeta = MetaManager.findClassMeta(fieldType);

		//-- If persistent but final or unannotated -> exception
		if(classMeta.isPersistentClass()) {
			if(ann == null)
				throw new ProgrammerErrorException("The field " + field + " must be annotated with @ReInject because it contains a persistent class, see " + WikiUrls.SUBPAGES);
			if(! ann.value())							// Explicitly forbidden to inject?
				return;
			if(isFinal)
				throw new ProgrammerErrorException("The field " + field + " cannot be final: it contains an entity class and is annotated with @ReInject, see " + WikiUrls.SUBPAGES);

			list.add(new SubFieldEntityInjector(field));
			return;
		}

		if(ann != null) {
			if(isFinal)
				throw new ProgrammerErrorException("The field " + field + " cannot be final: it is annotated with @ReInject, see " + WikiUrls.SUBPAGES);
			list.add(new SubFieldEntityInjector(field));
			return;
		}

		//-- Do we need a check injector? We only add those in development mode..
		if(! DomApplication.get().inDevelopmentMode())
			return;

		list.add(new SubFieldEntityCheckingInjector(field));
	}

	private Class<?> calculateFieldType(Field field) {
		Class<?> type = field.getType();
		Class<?> gtype = MetaManager.findCollectionType(type.getComponentType());
		if(null == gtype)
			return type;
		if(type.isAssignableFrom(gtype))
			return gtype;
		return type;
	}
}
