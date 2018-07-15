package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderScannerBase;

import java.lang.reflect.Type;

public class JsonLongFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull Class< ? > typeClass, @Nullable Type type) {
		if(typeClass == Long.class || typeClass == long.class) {
			return new ITypeMapping() {
				@Override
				public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
					Number n = (Number) instance;
					w.writeNumber(n);
				}

				@Override
				public Object parse(@NonNull JsonReader reader) throws Exception {
					if(reader.getLastToken() != ReaderScannerBase.T_NUMBER)
						throw new JsonParseException(reader, this, "Expecting a long integer but got " + reader.getTokenString());
					Long res = Long.decode(reader.getCopied());
					reader.nextToken();
					return res;
				}
			};
		}
		return null;
	}

}
