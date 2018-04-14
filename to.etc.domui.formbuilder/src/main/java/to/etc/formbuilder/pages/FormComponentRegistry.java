package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.graph.ColorPickerButton;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.panellayout.ILayoutPanel;
import to.etc.domui.component.panellayout.LayoutPanelBase;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.ClassPathScanner;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This singleton class will collect all DomUI components that are usable inside the form builder.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
final public class FormComponentRegistry {
	static private final FormComponentRegistry m_instance = new FormComponentRegistry();

	@NonNull
	private Set<Class< ? >> m_ignoreSet = new HashSet<Class< ? >>();

	@NonNull
	private List<IFbComponent> m_componentList = new ArrayList<IFbComponent>();

	@NonNull
	private Map<String, IFbComponent> m_byNameMap = new HashMap<String, IFbComponent>();

	public FormComponentRegistry() {
		registerComponent(Text.class);
		registerComponent(TextArea.class);
		registerComponent(DefaultButton.class);
		registerComponent(SmallImgButton.class);
		registerComponent(ColorPickerButton.class);
		registerComponent(LayoutPanelBase.class);
	}

	@NonNull
	public static FormComponentRegistry getInstance() {
		return m_instance;
	}

	public synchronized void register(@NonNull IFbComponent component, @Nullable Class< ? > rootClass) {
		List<IFbComponent> compl = new ArrayList<IFbComponent>(m_componentList);
		compl.add(component);
		m_componentList = Collections.unmodifiableList(compl);

		m_byNameMap.put(component.getTypeID(), component);
		if(null != rootClass) {
			m_byNameMap.put(rootClass.getName(), component);
		}
	}

	@NonNull
	public synchronized List<IFbComponent> getComponentList() {
		return m_componentList;
	}

	@Nullable
	public synchronized IFbComponent findComponent(@NonNull String name) {
		return m_byNameMap.get(name);
	}

	@Nullable
	public IFbComponent findComponent(@NonNull Class< ? > clz) {
		return m_byNameMap.get(clz.getName());
	}

	/**
	 * Auto-register a component class.
	 * @param componentClass
	 */
	public void registerComponent(@NonNull Class< ? > componentClass) {
		if(!m_ignoreSet.add(componentClass))
			return;
		if(Modifier.isAbstract(componentClass.getModifiers()))
			return;
		if(!Modifier.isPublic(componentClass.getModifiers()))
			return;

		if(IFbComponent.class.isAssignableFrom(componentClass)) {
			registerComponentHelper((Class< ? extends IFbComponent>) componentClass);
			return;
		}

		//-- Does this class have a companion class implementing the UI interface?
		String name = componentClass.getName();
		int lastindex = name.lastIndexOf('.');
		name = name.substring(0, lastindex + 1) + "Fb" + name.substring(lastindex + 2);
		Class< ? > altc = null;
		try {
			altc = componentClass.getClassLoader().loadClass(name);			// Try to load alternate class
		} catch(Exception x) {}

		if(altc != null) {
			if(IFbComponent.class.isAssignableFrom(altc)) {
				registerComponentHelper((Class< ? extends IFbComponent>) altc);
				return;
			}
			throw new IllegalStateException(componentClass + "'s companion class " + altc.getName() + " does not implement " + IFbComponent.class.getName());
		}

		//-- Get usable constructors.
		Constructor< ? > cons = null;
		try {
			cons = componentClass.getConstructor();
		} catch(Exception x) {
		}
		if(null == cons) {
			try {
				cons = componentClass.getConstructor(Class.class);
			} catch(Exception x) {
				throw new IllegalStateException(componentClass + " has no parameterless/Class<T> constructor");
			}
		}

		if(!NodeBase.class.isAssignableFrom(componentClass))
			throw new IllegalStateException(componentClass + " does not extend " + NodeBase.class.getName());

		//-- auto-register
		AutoComponent component = autoRegister((Class< ? extends NodeBase>) componentClass);

		//-- Only register if we can create instances without trouble
		try {
			component.checkInstantiation();
		} catch(Exception x) {
			System.out.println(componentClass.getName() + ": ignored because it cannot instantiate: " + x);
			return;
		}

		register(component, componentClass);
	}

	private AutoComponent autoRegister(@NonNull Class< ? extends NodeBase> componentClass) {
		if(ILayoutPanel.class.isAssignableFrom(componentClass))
			return new AutoLayout(componentClass);

		return new AutoComponent(componentClass);
	}

	private void registerComponentHelper(@NonNull Class< ? extends IFbComponent> componentClass) {
		// TODO Auto-generated method stub

	}

	public void scanComponents() throws Exception {
		ClassPathScanner csc = new ClassPathScanner();
		csc.addListener(new ClassPathScanner.IClassEntry() {
			@Override
			public void foundClass(@NonNull File source, @NonNull Class< ? > theClass) throws Exception {
//				System.out.println("Clz: " + theClass.getName());
				if(NodeBase.class.isAssignableFrom(theClass) && !UrlPage.class.isAssignableFrom(theClass)) {
//					System.out.println("Trying: " + theClass.getName());
					try {
						registerComponent(theClass);
					} catch(Exception x) {
						System.err.println(x.getMessage());
					}

				}
			}
		});
		csc.addClassloader(getClass().getClassLoader());
		csc.addClassloader(NodeBase.class.getClassLoader());
		csc.scan();
	}


	static {
	}


}
