package to.etc.domui.parts;

import javax.annotation.*;

import to.etc.domui.state.*;

public interface IComponentJsonProvider {
	@Nonnull
	public Object provideJsonData(@Nonnull IPageParameters parameterSource) throws Exception;
}
