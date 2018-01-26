package to.etc.json;

import java.lang.reflect.*;

import javax.annotation.*;

public interface IJsonTypeFactory {
	ITypeMapping createMapper(@Nonnull JsonTypeRegistry registry, @Nonnull Class<?> typeClass, @Nullable Type type);
}
