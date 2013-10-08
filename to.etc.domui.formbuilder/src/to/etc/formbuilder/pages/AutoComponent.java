package to.etc.formbuilder.pages;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.util.*;

/**
 * An auto-registered DomUI component: the class is just instantiated as a component and drawn as
 * such.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
public class AutoComponent implements IFbComponent {
	final private Class< ? extends NodeBase> m_componentClass;

	@Nonnull
	final private String m_shortName;

	public AutoComponent(Class< ? extends NodeBase> componentClass) {
		m_componentClass = componentClass;
		String name = componentClass.getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		Constructor< ? > cons = ClassUtil.findConstructor(componentClass, Class.class);	// Class<T> constructor?
		if(null != cons)
			name += "<T>";
		m_shortName = name;
	}

	@Override
	public void drawSelector(@Nonnull NodeContainer container) throws Exception {
		NodeBase instance = createInstance();
		container.add(instance);

	}

	@Override
	@Nonnull
	public String getShortName() {
		return m_shortName;
	}

	@Override
	@Nonnull
	public String getLongName() {
		return m_componentClass.getName();
	}

	/* Java sucks */
	@Nonnull
	static final private Class< ? >[] PARAMCLZ = {String.class, Integer.class, Boolean.class};


	@Nonnull
	private NodeBase createInstance() throws Exception {
		Constructor< ? > cons = ClassUtil.findConstructor(m_componentClass);	// Parameterless constructor?
		if(null != cons) {
			NodeBase base = (NodeBase) cons.newInstance();
			return base;
		}

		//-- Need class<T>
		cons = ClassUtil.findConstructor(m_componentClass, Class.class);	// Class<T> constructor?
		if(null == cons)
			throw new IllegalStateException(m_componentClass+": no idea how to make'un.");

		for(Class< ? > pc : PARAMCLZ) {
			try {
				NodeBase base = (NodeBase) cons.newInstance(pc);
				return base;
			} catch(Exception x) {}
		}
		throw new IllegalStateException(m_componentClass + ": no idea how to make'un.");
	}
}
