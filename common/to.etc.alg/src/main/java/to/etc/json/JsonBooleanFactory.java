package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderScannerBase;

import java.lang.reflect.Type;

public class JsonBooleanFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull Class< ? > typeClass, @Nullable Type type) {
		if(typeClass == Boolean.class || typeClass == boolean.class) {
			return new ITypeMapping() {
				@Override
				public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
					Boolean n = (Boolean) instance;
					w.write(n.toString());
				}

				@Override
				public Object parse(@NonNull JsonReader reader) throws Exception {
					boolean value;
					if(reader.getLastToken() == ReaderScannerBase.T_IDENT) {
						value = Boolean.parseBoolean(reader.getCopied());				// true/false
					} else if(reader.getLastToken() == ReaderScannerBase.T_NUMBER) {
						value = Long.parseLong(reader.getCopied()) != 0;
					} else
						throw new JsonParseException(reader, this, "Expecting a boolean value but got " + reader.getTokenString());
					reader.nextToken();
					return Boolean.valueOf(value);
				}
			};
		}
		return null;
	}
}
