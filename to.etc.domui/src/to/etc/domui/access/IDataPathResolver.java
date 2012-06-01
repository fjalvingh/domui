package to.etc.domui.access;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public interface IDataPathResolver {

	Object resolveDataPath(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull RequestContextImpl ctx, @Nonnull String target, @Nullable String dataPath) throws Exception;

}
