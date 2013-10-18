package to.etc.json;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonArrayType extends AbstractJsonArrayType implements ITypeMapping {
	@Nonnull
	private final Class< ? > m_implementationType;

	public JsonArrayType(@Nonnull ITypeMapping memberMapping, @Nonnull Class< ? > memberClass) {
		super(memberMapping);
		m_implementationType = memberClass;
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
		return new ArrayList<Object>();
	}

	@Override
	@Nonnull
	protected Iterator<Object> getIterator(@Nonnull Object instance) {
		return new ArrayIterator<Object>((Object[]) instance);
	}

	@Override
	protected Object convertResult(@Nonnull Collection<Object> res) throws Exception {
		Object[] val = (Object[]) Array.newInstance(m_implementationType, res.size());
		int index = 0;
		for(Object o : res) {
			val[index++] = o;
		}
		return val;
	}
}
