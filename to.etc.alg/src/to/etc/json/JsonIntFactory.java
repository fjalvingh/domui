package to.etc.json;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.lexer.*;

public class JsonIntFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull Class< ? > typeClass, @Nullable Type type) {
		if(typeClass == Integer.class || typeClass == int.class) {
			return new ITypeMapping() {
				@Override
				public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
					Number n = (Number) instance;
					w.writeNumber(n);
				}

				@Override
				public Object parse(@Nonnull JsonReader reader) throws Exception {
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
