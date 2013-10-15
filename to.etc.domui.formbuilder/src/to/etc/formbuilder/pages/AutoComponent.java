package to.etc.formbuilder.pages;

import java.io.*;
import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
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

	@Nullable
	private String m_selectorImage;

	@Nonnull
	final private String m_categoryName;

	public AutoComponent(Class< ? extends NodeBase> componentClass) {
		m_componentClass = componentClass;
		String fullname = componentClass.getName();

		int ldot = fullname.lastIndexOf('.');
		String name = fullname.substring(ldot + 1);

		Constructor< ? > cons = ClassUtil.findConstructor(componentClass, Class.class);	// Class<T> constructor?
		if(null != cons)
			name += "<T>";
		m_shortName = name;

		//-- Determine category from package name last entry
		int pdot = fullname.lastIndexOf('.', ldot - 1);
		name = StringTool.getCapitalized(fullname.substring(pdot + 1, ldot));
		m_categoryName = name;
	}

	@Override
	public String getTypeID() {
		return getLongName();
	}

	public void setSelectorImage(@Nonnull String image) {
		m_selectorImage = image;
	}

	@Nonnull
	public String getSelectorImage() {
		String si = m_selectorImage;
		if(null == si) {
			//-- Is there a class resource for this thingy?
			String name = "/"+m_componentClass.getName().replace(".", "/")+".png";
			if(hasClassResource(name)) {
				si = m_selectorImage = DomUtil.getJavaResourceRURL(m_componentClass, getShortName()+".png");
			} else {
				si = m_selectorImage = DomUtil.getJavaResourceRURL(getClass(), "autoComponent.png");
			}
		}

		return si;
	}

	private boolean hasClassResource(@Nonnull String ref) {
		InputStream is = m_componentClass.getResourceAsStream(ref);
		FileTool.closeAll(is);
		return is != null;
	}

	@Override
	public void drawSelector(@Nonnull NodeContainer container) throws Exception {
		Img img = new Img(getSelectorImage());
		container.add(img);
//
//		try {
//			NodeBase instance = createInstance();
//			instance.build();
//			container.add(instance);
//			instance.setVisibility(VisibilityType.HIDDEN);
//
//			container.appendCreateJS("window._fb.registerComponent('" + container.getActualID() + "','" + instance.getActualID() + "');");
//
//		} catch(Exception x) {
//			x.printStackTrace();
//		}
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

	@Override
	@Nonnull
	public String getCategoryName() {
		return m_categoryName;
	}

	/* Java sucks */
	@Nonnull
	static final private Class< ? >[] PARAMCLZ = {String.class, Integer.class, Boolean.class};

	public void checkInstantiation() throws Exception {
		NodeBase instance = createNodeInstance();
		instance.build();
	}

	@Override
	@Nonnull
	public NodeBase createNodeInstance() throws Exception {
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
//
//	@Override
//	public ComponentInstance createInstance() throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public String toString() {
		return "AutoComponent:" + getLongName();
	}
}
