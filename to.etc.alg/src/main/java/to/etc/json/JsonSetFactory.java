package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.ClassUtil;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class JsonSetFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull final Class< ? > typeClass, @Nullable Type type) {
		if(!Set.class.isAssignableFrom(typeClass) || type == null)
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
		Class< ? > def = SortedSet.class.isAssignableFrom(typeClass) ? TreeSet.class : HashSet.class;
		return new JsonCollectionType(memberMapping, JsonCollectionType.getImplementationClass(typeClass, def));
	}
}
