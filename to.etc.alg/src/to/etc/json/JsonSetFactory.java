package to.etc.json;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonSetFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull final Class< ? > typeClass, @Nullable Type type) {
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
