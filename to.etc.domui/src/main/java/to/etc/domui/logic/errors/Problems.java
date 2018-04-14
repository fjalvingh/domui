package to.etc.domui.logic.errors;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.PropertyMetaModel;

/**
 * Created by jal on 11/16/14.
 */
@Immutable
final public class Problems {
	private Problems() {}

	static public final <T> void mandatory(@NonNull ProblemModel model, @NonNull T instance, @NonNull String property) {
		MandatoryProblem.INSTANCE.check(model, instance, property);
	}

	static public final <T, V> void mandatory(@NonNull ProblemModel model, @NonNull T instance, @NonNull PropertyMetaModel<V> property) {
		MandatoryProblem.INSTANCE.check(model, instance, property);
	}
}
