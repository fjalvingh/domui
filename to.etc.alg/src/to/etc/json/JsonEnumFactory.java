package to.etc.json;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.lexer.*;
import to.etc.util.*;

public class JsonEnumFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull final Class< ? > typeClass, @Nullable Type type) {
		if(!Enum.class.isAssignableFrom(typeClass))
			return null;

		return new ITypeMapping() {
			@Override
			public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
				Enum< ? > en = (Enum< ? >) instance;
				w.writeString(en.name());
			}

			@Override
			public Object parse(@Nonnull JsonReader reader) throws Exception {
				if(reader.getLastToken() != ReaderScannerBase.T_STRING)
					throw new JsonParseException(reader, this, "Expecting a string (enum " + typeClass.getName() + ") but got " + reader.getTokenString());
				String val = StringTool.strUnquote(reader.getCopied());
				Class<Enum< ? >> enc = (Class<Enum< ? >>) typeClass;
				reader.nextToken();
				for(Enum< ? > ec : enc.getEnumConstants()) {
					if(ec.name().equals(val))
						return ec;
				}
				throw new JsonParseException(reader, this, "Enum value '" + val + "' not valid for " + typeClass);
			}
		};
	}
}
