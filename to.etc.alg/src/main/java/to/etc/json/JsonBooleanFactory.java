package to.etc.json;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.lexer.*;

public class JsonBooleanFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull Class< ? > typeClass, @Nullable Type type) {
		if(typeClass == Boolean.class || typeClass == boolean.class) {
			return new ITypeMapping() {
				@Override
				public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
					Boolean n = (Boolean) instance;
					w.write(n.toString());
				}

				@Override
				public Object parse(@Nonnull JsonReader reader) throws Exception {
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
