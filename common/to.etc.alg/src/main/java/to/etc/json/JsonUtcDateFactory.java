package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderScannerBase;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Maps java.util.Date to a long utc value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class JsonUtcDateFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull Class< ? > typeClass, @Nullable Type type) {
		if(Date.class.isAssignableFrom(typeClass)) {
			return new ITypeMapping() {

				@Override
				public void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception {
					Date dt = (Date) instance;
					w.writeLong(dt.getTime());
				}

				@Override
				public Object parse(@NonNull JsonReader reader) throws Exception {
					if(reader.getLastToken() != ReaderScannerBase.T_NUMBER)
						throw new JsonParseException(reader, this, "Expecting a number (UTC date) but got " + reader.getTokenString());
					long val = Long.parseLong(reader.getCopied());
					reader.nextToken();
					return new Date(val);
				}
			};
		}
		return null;
	}
}
