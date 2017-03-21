package to.etc.json;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

final class JsonCollectionType extends AbstractJsonArrayType implements ITypeMapping {
	@Nonnull
	private final Class<? extends Collection<?>>	m_implementationType;

	JsonCollectionType(@Nonnull ITypeMapping memberMapping, @Nonnull Class< ? extends Collection< ? >> implementationType) {
		super(memberMapping);
		m_implementationType = implementationType;
	}

	/**
	 * Create the best holding type for an input type for basic Collection types.
	 * @param typeClass
	 * @return
	 */
	@Nonnull
	static public Class< ? extends Collection< ? >> getImplementationClass(@Nonnull Class< ? > typeClass, @Nonnull Class< ? > defaultImplementation) {
		int mod = typeClass.getModifiers();
		if(!Modifier.isAbstract(mod) && Modifier.isPublic(mod) && !Modifier.isInterface(mod))
			return (Class< ? extends Collection< ? >>) typeClass;
		return (Class< ? extends Collection< ? >>) defaultImplementation;
	}

	@Override
	@Nonnull
	protected Collection< ? > createInstance() throws Exception {
		return m_implementationType.newInstance();
	}

	@Override
	@Nonnull
	protected Iterator<Object> getIterator(@Nonnull Object instance) {
		return ((Collection<Object>) instance).iterator();
	}
}