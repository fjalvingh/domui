package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;

public interface ITypeMapping {
	void render(@NonNull JsonWriter w, @NonNull Object instance) throws Exception;

	Object parse(@NonNull JsonReader reader) throws Exception;
}
