package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.IHtmlInput;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.DomUtil;
import to.etc.util.ClassUtil;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An auto-registered DomUI component: the class is just instantiated as a component and drawn as
 * such.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
public class AutoComponent implements IFbComponent {
	final private Class< ? extends NodeBase> m_componentClass;

	@NonNull
	final private String m_shortName;

	@Nullable
	private String m_selectorImage;

	@NonNull
	final private String m_categoryName;

	@NonNull
	final private Set<PropertyDefinition> m_propertySet;

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
		name = StringTool.strCapitalized(fullname.substring(pdot + 1, ldot));
		m_categoryName = name;

		m_propertySet = calculatePropertySet(componentClass);
	}

	@Override
	public String getTypeID() {
		return getLongName();
	}

	public void setSelectorImage(@NonNull String image) {
		m_selectorImage = image;
	}

	@NonNull
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

	private boolean hasClassResource(@NonNull String ref) {
		InputStream is = m_componentClass.getResourceAsStream(ref);
		FileTool.closeAll(is);
		return is != null;
	}

	@Override
	public void drawSelector(@NonNull NodeContainer container) throws Exception {
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
	@NonNull
	public String getShortName() {
		return m_shortName;
	}

	@Override
	@NonNull
	public String getLongName() {
		return m_componentClass.getName();
	}

	@Override
	@NonNull
	public String getCategoryName() {
		return m_categoryName;
	}

	/* Java sucks */
	@NonNull
	static final private Class< ? >[] PARAMCLZ = {String.class, Integer.class, Boolean.class};

	public void checkInstantiation() throws Exception {
		NodeBase instance = createNodeInstance();
		instance.build();
	}

	@Override
	@NonNull
	public NodeBase createNodeInstance() throws Exception {
		Constructor< ? > cons = ClassUtil.findConstructor(m_componentClass);	// Parameterless constructor?
		if(null != cons) {
			NodeBase base = (NodeBase) cons.newInstance();
			return convertToPainter(base);
		}

		//-- Need class<T>
		cons = ClassUtil.findConstructor(m_componentClass, Class.class);	// Class<T> constructor?
		if(null == cons)
			throw new IllegalStateException(m_componentClass+": no idea how to make'un.");

		for(Class< ? > pc : PARAMCLZ) {
			try {
				NodeBase base = (NodeBase) cons.newInstance(pc);
				return convertToPainter(base);
			} catch(Exception x) {}
		}
		throw new IllegalStateException(m_componentClass + ": no idea how to make'un.");
	}

	public NodeBase convertToPainter(@NonNull NodeBase node) {
		forcePainterView(node);

		return node;
//		Div d = new Div();
//		d.setCssClass("fb-ui-test");
//		d.add(node);
//		forcePainterView(node);
//
//		return d;
	}


	public void forcePainterView(@NonNull NodeBase node) {
		if(node instanceof IHtmlInput) {
//			((IHtmlInput) node).setDisabled(true);
//			node.setSpecialAttribute("onfocus", "return false;");
			node.addCssClass("fb-ui-paint");
		}
		if(node instanceof Button) {
			Button b = (Button) node;
//			b.setSpecialAttribute("onfocus", "return false;");
			b.addCssClass("fb-ui-button");
		}

		if(node instanceof NodeContainer) {
			for(NodeBase nc : ((NodeContainer) node)) {
				forcePainterView(nc);
			}
		}
	}

	/**
	 * Return all editable properties for a component of this type.
	 *
	 * @see to.etc.formbuilder.pages.IFbComponent#getProperties()
	 */
	@Override
	public Set<PropertyDefinition> getProperties() {
		return m_propertySet;
	}

	@NonNull
	private Set<PropertyDefinition> calculatePropertySet(@NonNull Class< ? extends NodeBase> componentClass) {
		List<PropertyMetaModel< ? >> prl = MetaManager.findClassMeta(componentClass).getProperties();
		Set<PropertyDefinition> res = new HashSet<PropertyDefinition>();
		for(PropertyMetaModel< ? > pmm : prl) {
			PropertyDefinition pd = createDefinition(pmm);
			if(null != pd)
				res.add(pd);
		}
		return res;
	}

//
//	@Override
//	public ComponentInstance createInstance() throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Nullable
	private PropertyDefinition createDefinition(@NonNull PropertyMetaModel< ? > pmm) {
		if(pmm.getReadOnly() == YesNoType.YES)
			return null;
		if(PropertyDefinition.isIgnored(pmm.getName()))
			return null;

		IPropertyEditorFactory fact = DefaultPropertyEditorFactory.INSTANCE;
		if(pmm.getDomainValues() != null) {
			//-- Domain-specific thing. Create combo thing.
			fact = ComboPropertyEditorFactory.createFactory(pmm);
		} else if(pmm.getName().toLowerCase().contains("color")) {
			fact = ColorPropertyEditorFactory.INSTANCE;
		}


		PropertyDefinition pd = PropertyDefinition.getDefinition(pmm.getActualType(), pmm.getName(), calculateCategory(pmm), fact);
		return pd;
	}

	/**
	 * Tries to categorize.
	 * @param pmm
	 * @return
	 */
	@NonNull
	private String calculateCategory(@NonNull PropertyMetaModel< ? > pmm) {
		return PropertyDefinition.getCategory(pmm.getName());
	}

	@Override
	public String toString() {
		return "AutoComponent:" + getLongName();
	}
}
