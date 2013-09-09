package to.etc.domui.parts;

import javax.annotation.*;

import to.etc.domui.server.*;

public interface IComponentUrlDataProvider {
	public void provideUrlData(@Nonnull RequestContextImpl parameterSource) throws Exception;
}
