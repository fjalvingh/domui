package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.util.ArrayIterator;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class JsonArrayType extends AbstractJsonArrayType implements ITypeMapping {
	@NonNull
	private final Class< ? > m_implementationType;

	public JsonArrayType(@NonNull ITypeMapping memberMapping, @NonNull Class< ? > memberClass) {
		super(memberMapping);
		m_implementationType = memberClass;
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
		return new ArrayList<Object>();
	}

	@Override
	@NonNull
	protected Iterator<Object> getIterator(@NonNull Object instance) {
		return new ArrayIterator<Object>((Object[]) instance);
	}

	@Override
	protected Object convertResult(@NonNull Collection<Object> res) throws Exception {
		Object[] val = (Object[]) Array.newInstance(m_implementationType, res.size());
		int index = 0;
		for(Object o : res) {
			val[index++] = o;
		}
		return val;
	}
}
