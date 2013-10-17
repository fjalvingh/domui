package to.etc.webapp.json;

import javax.annotation.*;

public class JsonIntType implements ITypeMapping {

	@Override
	public void render(@Nonnull JsonWriter w, @Nonnull Object instance) throws Exception {
		Number n = (Number) instance;
		w.writeNumber(n);
	}

	@Override
	public Object parse(@Nonnull JsonReader reader) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
