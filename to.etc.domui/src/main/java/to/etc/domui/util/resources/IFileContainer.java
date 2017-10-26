package to.etc.domui.util.resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IFileContainer {
	@Nullable
	IModifyableResource findFile(@Nonnull String name);

	List<String> getInventory();
}
