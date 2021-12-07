package to.etc.domui.logic.errors;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.util.Msgs;

/**
 * This problem helper checks mandatoryness on fields.
 * Created by jal on 11/16/14.
 */
final public class MandatoryProblem extends Problem {
	private static final Logger LOG = LoggerFactory.getLogger(MandatoryProblem.class);

	static public final MandatoryProblem	INSTANCE = new MandatoryProblem();

	private MandatoryProblem() {
		super(Msgs.mandatory);
	}

	public <T> void check(@NonNull ProblemModel model, @NonNull T instance, @NonNull String property) {
		check(model, instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	public <T, V> void check(@NonNull ProblemModel model, @NonNull T instance, @NonNull PropertyMetaModel<V> property) {
		off(model, instance, property);
		try {
			V value = property.getValue(instance);
			if(null != value)
				return;
		} catch(Exception x) {
			LOG.error("MandatoryProblem exception: " + x, x);
		}
		//-- Exception or null value -> problem
		on(model, instance, property);
	}
}
