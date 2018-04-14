package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.lexer.ReaderScannerBase;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

abstract public class AbstractJsonArrayType implements ITypeMapping {
	@NonNull
	private final ITypeMapping m_memberMapping;

	@NonNull
	abstract protected Iterator<Object> getIterator(@NonNull Object instance);

	@NonNull
	abstract protected Collection< ? > createInstance() throws Exception;

	protected AbstractJsonArrayType(@NonNull ITypeMapping memberMapping) {
		m_memberMapping = memberMapping;
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
	public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
		Collection<Object> coll = (Collection<Object>) instance;
		w.write('[');
		w.inc();
		int ct = 0;
		for(Iterator<Object> it = getIterator(instance); it.hasNext();) {
			Object o = it.next();

			if(ct++ > 0) {
				w.write(',');
			} else if(ct % 10 == 0) {
				w.nl();
			}
			if(null == o) {
				w.write("null");
			} else {
				m_memberMapping.render(w, o);
			}
		}
		w.dec();
		w.write("]");
	}

	@Override
	public Object parse(@NonNull JsonReader reader) throws Exception {
		if(reader.getLastToken() == ReaderScannerBase.T_IDENT) {
			//-- We can have null here.
			if("null".equalsIgnoreCase(reader.getCopied())) {
				reader.nextToken();
				return null;
			}
		} else if(reader.getLastToken() == '[') {
			reader.nextToken();
			return parseList(reader);
		}
		throw new JsonParseException(reader, this, "Expecting a json array '[' but got " + reader.getTokenString());
	}

	private Object parseList(@NonNull JsonReader reader) throws Exception {
		//-- 1. Instantiate the data thing.
		Collection<Object> collection = (Collection<Object>) createInstance();
		for(;;) {
			int token = reader.getLastToken();
			if(token == ']')
				break;
			if(token == ReaderScannerBase.T_EOF)
				throw new JsonParseException(reader, this, "Unexpected eof while parsing json array");
			Object value = m_memberMapping.parse(reader);
			collection.add(value);

			//-- Skip to next element
			token = reader.getLastToken();
			if(token == ',') {
				reader.nextToken();
			} else if(token == ']')
				break;
			else
				throw new JsonParseException(reader, this, "Expecting either ] or , but got " + reader.getTokenString());
		}
		reader.nextToken();										// Skip ]
		return convertResult(collection);
	}

	protected Object convertResult(@NonNull Collection<Object> res) throws Exception {
		return res;
	}
}

