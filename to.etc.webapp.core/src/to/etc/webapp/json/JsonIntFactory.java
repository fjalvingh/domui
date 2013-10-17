package to.etc.webapp.json;

import java.lang.reflect.*;

import javax.annotation.*;

public class JsonIntFactory implements IJsonTypeFactory {
	@Override
	public ITypeMapping createMapper(@Nonnull Class< ? > typeClass, @Nullable Type type) {
		if(typeClass == Integer.class || typeClass == int.class) {
			return new JsonIntType();
		}
		return null;
	}

}
