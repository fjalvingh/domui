package to.etc.json;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.lexer.*;
import to.etc.util.*;

public class JsonStringFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull Class< ? > typeClass, @Nullable Type type) {
		if(String.class == typeClass) {
			return new ITypeMapping() {

				@Override
				public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
					w.writeString((String) instance);
				}

				@Override
				public Object parse(@Nonnull JsonReader reader) throws Exception {
					if(reader.getLastToken() != ReaderScannerBase.T_STRING)
						throw new JsonParseException(reader, this, "Expecting a string but got " + reader.getTokenString());
					String res = StringTool.strUnquote(reader.getCopied());
					reader.nextToken();
					return res;
				}
			};

		}
		return null;
	}
}
