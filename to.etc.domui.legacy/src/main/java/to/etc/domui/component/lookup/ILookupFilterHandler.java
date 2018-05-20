package to.etc.domui.component.lookup;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QDataContext;

import java.util.List;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/27/16.
 */
public interface ILookupFilterHandler {

	@NonNull Long save(@NonNull QDataContext dc, @NonNull String pageName, @NonNull String filterName, @NonNull String filterContent) throws Exception;

	@NonNull List<SavedFilter> load(@NonNull QDataContext dc, @NonNull String pageName) throws Exception;

	void delete(@NonNull QDataContext dc, @NonNull Long recordId) throws Exception;

}
