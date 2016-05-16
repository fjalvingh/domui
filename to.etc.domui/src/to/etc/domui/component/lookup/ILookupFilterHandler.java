package to.etc.domui.component.lookup;

import java.util.*;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/27/16.
 */
public interface ILookupFilterHandler {

	@Nonnull Long save(@Nonnull QDataContext dc, @Nonnull String pageName, @Nonnull String filterName, @Nonnull String filterContent) throws Exception;

	@Nonnull List<SavedFilter> load(@Nonnull QDataContext dc, @Nonnull String pageName) throws Exception;

	void delete(@Nonnull QDataContext dc, @Nonnull Long recordId) throws Exception;

}
