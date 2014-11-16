package to.etc.domui.logic.errors;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

import javax.annotation.*;

/**
 * This problem helper checks mandatoryness on fields.
 * Created by jal on 11/16/14.
 */
final public class MandatoryProblem extends Problem {
	static public final MandatoryProblem	INSTANCE = new MandatoryProblem();

	private MandatoryProblem() {
		super(Msgs.class, Msgs.MANDATORY);
	}

	public <T> void check(@Nonnull ProblemModel model, @Nonnull T instance, @Nonnull String property) {
		check(model, instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	public <T, V> void check(@Nonnull ProblemModel model, @Nonnull T instance, @Nonnull PropertyMetaModel<V> property) {
		off(model, instance, property);
		try {
			V value = property.getValue(instance);
			if(null != value)
				return;
		} catch(Exception x) {
			x.printStackTrace();							// FIXME For now use this to detect this occurrence; it should be not shown later.
		}
		//-- Exception or null value -> problem
		on(model, instance, property);
	}
}
