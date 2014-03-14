package to.etc.json;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.lexer.*;

abstract public class AbstractJsonArrayType implements ITypeMapping {
	@Nonnull
	private final ITypeMapping m_memberMapping;

	@Nonnull
	abstract protected Iterator<Object> getIterator(@Nonnull Object instance);

	@Nonnull
	abstract protected Collection< ? > createInstance() throws Exception;

	protected AbstractJsonArrayType(@Nonnull ITypeMapping memberMapping) {
		m_memberMapping = memberMapping;
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
	public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
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
	public Object parse(@Nonnull JsonReader reader) throws Exception {
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

	private Object parseList(@Nonnull JsonReader reader) throws Exception {
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

	protected Object convertResult(@Nonnull Collection<Object> res) throws Exception {
		return res;
	}
}

