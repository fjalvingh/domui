package to.etc.domui.component.meta.init;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;
import to.etc.domui.component.meta.impl.DefaultPropertyMetaModel;
import to.etc.util.ClassUtil;
import to.etc.util.PropertyInfo;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides Java class properties. Must run asap after metamodel construction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
public class MIClassProperties implements IClassMetaProvider<DefaultClassMetaModel> {
	@Override
	public void provide(@NonNull MetaInitContext context, @NonNull DefaultClassMetaModel cmm) throws Exception {
		if(cmm.getActualClass().isRecord()) {
			// The idiots that implemented records of course did not obey their equally idiotic bean standard.
			calculateRecordFields(context, cmm);
		} else {
			calculateBeanFields(context, cmm);
		}
	}

	private void calculateRecordFields(MetaInitContext context, DefaultClassMetaModel cmm) {
		List<PropertyMetaModel<?>> reslist = new ArrayList<>(cmm.getActualClass().getRecordComponents().length);
		for(RecordComponent recordComponent : cmm.getActualClass().getRecordComponents()) {
			Class<?> actualType = recordComponent.getType();
			ClassMetaModel propertyPmm = context.getModel(actualType);        // Might defer execution

			DefaultPropertyMetaModel<?> pm = new DefaultPropertyMetaModel<>(cmm, recordComponent, propertyPmm);
			reslist.add(pm);
		}
		cmm.setClassProperties(reslist);
	}

	private static void calculateBeanFields(@NonNull MetaInitContext context, @NonNull DefaultClassMetaModel cmm) {
		List<PropertyInfo> pilist = ClassUtil.calculateProperties(cmm.getActualClass(), false);
		List<PropertyMetaModel<?>> reslist = new ArrayList<>(pilist.size());
		for(PropertyInfo pd : pilist) {
			if(!pd.getName().equals("class")) {
				Method rm = pd.getGetter();
				if(rm.getParameterTypes().length != 0)
					continue;

				Class<?> actualType = pd.getActualType();
				ClassMetaModel propertyPmm = context.getModel(actualType);        // Might defer execution

				DefaultPropertyMetaModel<?> pm = new DefaultPropertyMetaModel<>(cmm, pd, propertyPmm);
				reslist.add(pm);
				//colli.getMap().put(pd, pm);
			}
		}

		cmm.setClassProperties(reslist);
	}

}
