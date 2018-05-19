package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

final class JsonCollectionType extends AbstractJsonArrayType implements ITypeMapping {
	@NonNull
	private final Class<? extends Collection<?>>	m_implementationType;

	JsonCollectionType(@NonNull ITypeMapping memberMapping, @NonNull Class< ? extends Collection< ? >> implementationType) {
		super(memberMapping);
		m_implementationType = implementationType;
	}

	/**
	 * Create the best holding type for an input type for basic Collection types.
	 * @param typeClass
	 * @return
	 */
	@NonNull
	static public Class< ? extends Collection< ? >> getImplementationClass(@NonNull Class< ? > typeClass, @NonNull Class< ? > defaultImplementation) {
		int mod = typeClass.getModifiers();
		if(!Modifier.isAbstract(mod) && Modifier.isPublic(mod) && !Modifier.isInterface(mod))
			return (Class< ? extends Collection< ? >>) typeClass;
		return (Class< ? extends Collection< ? >>) defaultImplementation;
	}

	@Override
	@NonNull
	protected Collection< ? > createInstance() throws Exception {
		return m_implementationType.newInstance();
	}

	@Override
	@NonNull
	protected Iterator<Object> getIterator(@NonNull Object instance) {
		return ((Collection<Object>) instance).iterator();
	}
}
