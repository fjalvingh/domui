package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.impl.DefaultClassMetaModel;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-10-17.
 */
abstract public class AbstractClassAnnotationProvider implements IClassMetaProvider<DefaultClassMetaModel> {
	abstract protected void decodeClassAnnotation(MetaInitContext context, DefaultClassMetaModel cmm, Annotation an, String ana) throws Exception;

	@OverridingMethodsMustInvokeSuper
	@Override public void provide(@Nonnull MetaInitContext context, @Nonnull DefaultClassMetaModel cmm) throws Exception {
		Annotation[] annar = cmm.getActualClass().getAnnotations();
		for(Annotation an : annar) {
			String ana = an.annotationType().getName(); // Get the annotation's name
			decodeClassAnnotation(context, cmm, an, ana);
		}

		//-- Some annotations can be on parent classes.
		Class< ? > parentClass = cmm.getActualClass();
		for(;;) {
			parentClass = parentClass.getSuperclass();
			if(parentClass == Object.class || parentClass == null)
				break;

			annar = parentClass.getAnnotations();
			for(Annotation an : annar) {
				String ana = an.annotationType().getName();				// Get the annotation's name
				decodeParentClassAnnotation(context, cmm, an, ana);
			}
		}
	}

	protected void decodeParentClassAnnotation(MetaInitContext context, DefaultClassMetaModel cmm, Annotation an, String ana) throws Exception {
		decodeClassAnnotation(context, cmm, an, ana);
	}
}
