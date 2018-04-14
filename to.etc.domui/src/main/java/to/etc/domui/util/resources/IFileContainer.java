package to.etc.domui.util.resources;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
	IModifyableResource findFile(@NonNull String name);

	@NonNull
	List<String> getInventory();
}
