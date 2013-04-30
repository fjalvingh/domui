package to.etc.domui.databinding;

import javax.annotation.*;

public interface IUniConverter<A, B> {
	@Nullable
	public B convertSourceToTarget(@Nullable A source) throws Exception;
}
