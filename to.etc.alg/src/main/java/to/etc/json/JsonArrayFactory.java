package to.etc.json;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonArrayFactory  implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull final Class< ? > typeClass, @Nullable Type type) {
		if(! typeClass.isArray())
			return null;
		final Class< ? > memberType = ClassUtil.findCollectionType(typeClass);
		if(null == memberType || Object.class == memberType)
			return null;

		int mod = memberType.getModifiers();
		if(Modifier.isAbstract(mod) || Modifier.isInterface(mod) || !Modifier.isPublic(mod))
			return null;
		final ITypeMapping memberMapping = registry.createMapping(memberType, null);
		if(null == memberMapping)
			return null;
		return new JsonArrayType(memberMapping, memberType);
	}
}
