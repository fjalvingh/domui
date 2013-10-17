package to.etc.webapp.json;

import java.lang.reflect.*;

import javax.annotation.*;

public interface IJsonTypeFactory {
	public ITypeMapping createMapper(@Nonnull Class< ? > typeClass, @Nullable Type type);
}
