package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderScannerBase;

import java.lang.reflect.Type;

public class JsonIntFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull Class< ? > typeClass, @Nullable Type type) {
		if(typeClass == Integer.class || typeClass == int.class) {
			return new ITypeMapping() {
				@Override
				public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
					Number n = (Number) instance;
					w.writeNumber(n);
				}

				@Override
				public Object parse(@NonNull JsonReader reader) throws Exception {
					if(reader.getLastToken() != ReaderScannerBase.T_NUMBER)
						throw new JsonParseException(reader, this, "Expecting an integer but got " + reader.getTokenString());
					Integer res = Integer.decode(reader.getCopied());
					reader.nextToken();
					return res;
				}
			};
		}
		return null;
	}
}
