package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderScannerBase;
import to.etc.util.StringTool;

import java.lang.reflect.Type;

public class JsonStringFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull Class< ? > typeClass, @Nullable Type type) {
		if(String.class == typeClass) {
			return new ITypeMapping() {

				@Override
				public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
					w.writeString((String) instance);
				}

				@Override
				public Object parse(@NonNull JsonReader reader) throws Exception {
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
