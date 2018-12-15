package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

/**
 * Decodes the URLContextString from pages, and converts it to something
 * edible/injectable for a page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-12-18.
 */
public interface IUrlContextDecoder {
	@Nullable
	Map<String, Object> getContextValues(@NonNull IRequestContext ctx) throws Exception;
}
