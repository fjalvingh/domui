package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.DefaultClassMetaModel;
import to.etc.domui.component.meta.impl.DefaultPropertyMetaModel;
import to.etc.util.ClassUtil;
import to.etc.util.PropertyInfo;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides Java class properties. Must run asap after metamodel construction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-10-17.
 */
public class MIClassProperties implements IClassMetaProvider<DefaultClassMetaModel> {
	@Override public void provide(@Nonnull MetaInitContext context, @Nonnull DefaultClassMetaModel cmm) throws Exception {
		List<PropertyInfo> pilist = ClassUtil.calculateProperties(cmm.getActualClass(), false);
		List<PropertyMetaModel< ? >> reslist = new ArrayList<PropertyMetaModel< ? >>(pilist.size());
		for(PropertyInfo pd : pilist) {
			if(!pd.getName().equals("class")) {
				Method rm = pd.getGetter();
				if(rm.getParameterTypes().length != 0)
					continue;

				Class<?> actualType = pd.getActualType();
				ClassMetaModel propertyPmm = context.getModel(actualType);		// Might defer execution

				DefaultPropertyMetaModel< ? > pm = new DefaultPropertyMetaModel<>(cmm, pd, propertyPmm);
				reslist.add(pm);
				//colli.getMap().put(pd, pm);
			}
		}

		cmm.setClassProperties(reslist);
	}

}
