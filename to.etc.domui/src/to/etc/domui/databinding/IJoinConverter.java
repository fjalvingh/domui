package to.etc.domui.databinding;

import javax.annotation.*;

public interface IJoinConverter<A, B> extends IUniConverter<A, B> {
	@Nullable
	public A convertTargetToSource(@Nullable B target) throws Exception;
}
