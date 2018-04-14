package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Type;

public interface IJsonTypeFactory {
	ITypeMapping createMapper(@NonNull JsonTypeRegistry registry, @NonNull Class<?> typeClass, @Nullable Type type);
}
