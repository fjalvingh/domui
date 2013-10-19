package to.etc.json;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.lexer.*;
import to.etc.util.*;

public class JsonClassType<T> implements ITypeMapping {
	private Class<T> m_rootClass;

	private Map<String, PropertyMapping> m_map;

	public JsonClassType(@Nonnull Class<T> rootClass) {
		m_rootClass = rootClass;
	}

	@Override
	public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
		w.write("{");
		w.nl();
		w.inc();
		int ct = 0;
		for(PropertyMapping pm : m_map.values()) {
			Object value;
			try {
				value = pm.getGetter().invoke(instance);
			} catch(Exception x) {
				Exception nx = WrappedException.unwrap(x);
				throw new RuntimeException("JSON encode failed for " + pm.getName() + ": " + nx, nx);
			}

			if(null != value) {
				if(ct++ > 0)
					w.write(",");
				w.append(pm.getName());
				w.append(':');
				pm.getMapper().render(w, value);
				w.nl();
			}
		}
		w.dec();
		w.write("}");
	}

	@Override
	public Object parse(@Nonnull JsonReader reader) throws Exception {
		if(reader.getLastToken() != '{') {
			throw new JsonParseException(reader, this, "Expecting '{' but got " + reader.getLastToken());
		}
		reader.nextToken();

		//-- Create the instance.
		T instance = m_rootClass.newInstance();

		for(;;) {
			//-- 1. Expect key mappable to a property of this class.
			int token = reader.getLastToken();
			if(token == ReaderScannerBase.T_EOF)
				throw new JsonParseException(reader, this, "Unexpected eof");
			if(token == '}')
				break;

			String name;
			if(token == ReaderScannerBase.T_IDENT) {
				name = reader.getCopied();
			} else if(token == ReaderScannerBase.T_STRING) {
				name = StringTool.strUnquote(reader.getCopied());
			} else
				throw new JsonParseException(reader, this, "Expecting a property name, got " + (char) token);

			//-- Got a thingy. Must map to a class property.
			PropertyMapping pm = m_map.get(name);
			if(null == pm)
				throw new JsonParseException(reader, this, "JSON property '" + name + "' is not mapped on class " + m_rootClass.getName());

			//-- Next must be ':'
			token = reader.nextToken();
			if(token != ':')
				throw new JsonParseException(reader, this, "Missing ':' after property " + name);

			reader.nextToken();										// Prepare for parser.
			Object value = pm.getMapper().parse(reader);			// Parse property value
			Method setter = pm.getSetter();
			if(null != setter) {
				try {
					value = setter.invoke(instance, value);
				} catch(Exception x) {
					Exception nx = WrappedException.unwrap(x);
					throw new RuntimeException("JSON decode failed for " + pm.getName() + " value " + value + ": " + nx, nx);
				}
			}

			//--
			token = reader.getLastToken();
			if(token == '}')
				break;
			else if(token != ',')
				throw new JsonParseException(reader, this, "Missing ',' after property:value " + name);
			reader.nextToken();
		}
		reader.nextToken();
		return instance;
	}

	public void setMap(@Nonnull Map<String, PropertyMapping> res) {
		m_map = res;
	}
}
