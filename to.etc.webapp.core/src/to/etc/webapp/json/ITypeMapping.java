package to.etc.webapp.json;

import javax.annotation.*;

public interface ITypeMapping {
	public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception;

	public Object parse(@Nonnull JsonReader reader) throws Exception;
}
