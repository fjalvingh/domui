package to.etc.launcher.collector;

import javax.annotation.*;

public interface IArtefactMatcher {
	boolean accept(@Nonnull String packagePath);

}
