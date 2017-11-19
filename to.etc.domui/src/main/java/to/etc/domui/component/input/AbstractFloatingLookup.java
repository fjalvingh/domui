package to.etc.domui.component.input;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;

/**
 * Super class for floating window based lookups.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 16 Jun 2011
 */
public class AbstractFloatingLookup<T> extends FloatingWindow {
	/**
	 * The result class. For Java classes this usually also defines the metamodel to use; for generic meta this should
	 * be the value record class type.
	 */
	final private Class<T> m_lookupClass;

	/**
	 * The metamodel to use to handle the data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	final private ClassMetaModel m_metaModel;

	/**
	 * Lookup a POJO Java bean persistent class.
	 * @param lookupClass
	 */
	public AbstractFloatingLookup(Class<T> lookupClass) {
		this(lookupClass, null);
	}

	public AbstractFloatingLookup(Class<T> lookupClass, ClassMetaModel metaModel) {
		this(true, null, lookupClass, metaModel);
	}

	/**
	 * Create a floating window with the specified title in the title bar.
	 * @param txt
	 */
	protected AbstractFloatingLookup(boolean modal, String txt, Class<T> lookupClass, ClassMetaModel metaModel) {
		super(modal, txt);
		m_lookupClass = lookupClass;
		m_metaModel = metaModel != null ? metaModel : MetaManager.findClassMeta(lookupClass);
	}

	/**
	 * Default T. When set, table result would be stretched to use entire available height on FloatingWindow.
	 */
	private boolean m_useStretchedLayout = true;

	public Class<T> getLookupClass() {
		return m_lookupClass;
	}

	public ClassMetaModel getMetaModel() {
		return m_metaModel;
	}

	/**
	 * Returns T if we are using stretching of result table height to all remained parent height.
	 */
	public boolean isUseStretchedLayout() {
		return m_useStretchedLayout;
	}

	/**
	 * Set to F to disable stretching of result table height.
	 * @param useStretchedLayout
	 */
	public void setUseStretchedLayout(boolean useStretchedLayout) {
		m_useStretchedLayout = useStretchedLayout;
	}
}
