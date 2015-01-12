package to.etc.domui.logic.errors;

import to.etc.domui.component.meta.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * Created by jal on 11/16/14.
 */
@Immutable
final public class Problems {
	private Problems() {}

	static public final <T> void mandatory(@Nonnull ProblemModel model, @Nonnull T instance, @Nonnull String property) {
		MandatoryProblem.INSTANCE.check(model, instance, property);
	}

	static public final <T, V> void mandatory(@Nonnull ProblemModel model, @Nonnull T instance, @Nonnull PropertyMetaModel<V> property) {
		MandatoryProblem.INSTANCE.check(model, instance, property);
	}
}
