package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.ClassUtil;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders generic lists.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class JsonListFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull final Class< ? > typeClass, @Nullable Type type) {
		if(!List.class.isAssignableFrom(typeClass) || type == null)
			return null;
		final Class< ? > memberType = ClassUtil.findCollectionType(type);
		if(null == memberType || Object.class == memberType)
			return null;
		int mod = memberType.getModifiers();
		if(Modifier.isAbstract(mod) || Modifier.isInterface(mod) || !Modifier.isPublic(mod))
			return null;
		final ITypeMapping memberMapping = registry.createMapping(memberType, null);
		if(null == memberMapping)
			return null;

		return new JsonCollectionType(memberMapping, JsonCollectionType.getImplementationClass(typeClass, ArrayList.class));
	}
}
