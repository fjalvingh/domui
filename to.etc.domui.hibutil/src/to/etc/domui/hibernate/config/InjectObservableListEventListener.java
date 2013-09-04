package to.etc.domui.hibernate.config;

import java.lang.reflect.*;
import java.util.*;

import org.hibernate.event.*;

import to.etc.domui.databinding.*;
import to.etc.util.*;

public class InjectObservableListEventListener implements PostLoadEventListener {
	@Override
	public void onPostLoad(PostLoadEvent event) {
		Method[] mar = event.getEntity().getClass().getMethods();
		try {
			for(int i = mar.length; --i >= 0;) {
				Method m = mar[i];
				if(List.class.isAssignableFrom(m.getReturnType()) && m.getParameterTypes().length == 0) {
					if(m.getName().startsWith("get") && Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
						handleModifier(event.getEntity(), m);
					}
				}
			}
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	private void handleModifier(Object entity, Method m) throws Exception {
		//-- Locate the setter.
		Method sm = entity.getClass().getMethod("set" + m.getName().substring(3), List.class);

		List<Object> orig = (List<Object>) m.invoke(entity);
		if(null == orig)
			orig = new ArrayList<Object>();
		ObservableList< ? > newlist = new ObservableList<Object>(orig);
		sm.invoke(entity, newlist);

		String cn = entity.getClass().getName();
		cn = cn.substring(cn.lastIndexOf('.') + 1);
		System.out.println("Injecting " + cn + "." + m.getName());
	}
}
