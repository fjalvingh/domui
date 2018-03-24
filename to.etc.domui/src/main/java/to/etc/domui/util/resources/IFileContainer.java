package to.etc.domui.util.resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A container for class and resource files on the classpath. Two implementations exist,
 * one for jar files and the other for class directories.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IFileContainer {
	@Nullable
	IModifyableResource findFile(@Nonnull String name);

	@Nonnull
	List<String> getInventory();
}
