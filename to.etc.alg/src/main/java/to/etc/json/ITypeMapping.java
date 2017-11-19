package to.etc.json;

import javax.annotation.*;

public interface ITypeMapping {
	void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception;

	Object parse(@Nonnull JsonReader reader) throws Exception;
}
