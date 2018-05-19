package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderScannerBase;
import to.etc.util.StringTool;

import java.lang.reflect.Type;

public class JsonEnumFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull final Class< ? > typeClass, @Nullable Type type) {
		if(!Enum.class.isAssignableFrom(typeClass))
			return null;

		return new ITypeMapping() {
			@Override
			public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
				Enum< ? > en = (Enum< ? >) instance;
				w.writeString(en.name());
			}

			@Override
			public Object parse(@NonNull JsonReader reader) throws Exception {
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
