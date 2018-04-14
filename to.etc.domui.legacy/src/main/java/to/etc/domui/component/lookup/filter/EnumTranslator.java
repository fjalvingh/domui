package to.etc.domui.component.lookup.filter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Node;
import to.etc.domui.component.meta.MetaManager;
import to.etc.webapp.query.QDataContext;
import to.etc.xml.DomTools;
import to.etc.xml.XmlWriter;

/**
 * Translates an Enum value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@NonNullByDefault
class EnumTranslator<T extends Enum<T>> implements ITranslator<T> {

	private static final String CLASS = "class";

	@Override
	@Nullable
	public T deserialize(QDataContext dc, Node node) throws Exception {
		Node valueNode = DomTools.nodeFind(node, VALUE);
		String value = DomTools.textFrom(valueNode);
		if(null == value)
			return null;
		String className = DomTools.strAttr(valueNode, CLASS);
		return createEnumValue(className, value);
	}

	private <T extends Enum<T>> T createEnumValue(String clzName, String value) throws ClassNotFoundException {
		Class<T> clzMeta = (Class<T>) getClass().getClassLoader().loadClass(clzName);
		Class<T> clz = (Class<T>) MetaManager.findClassMeta(clzMeta).getActualClass();
		return Enum.valueOf(clz, value);
	}

	@Override
	public boolean serialize(XmlWriter writer, Object o) throws Exception {
		if(o instanceof Enum<?>) {
			writer.tag(VALUE, CLASS, o.getClass().getCanonicalName() );
			writer.write(((Enum<?>) o).name());
			writer.tagendnl();
			return true;
		}
		return false;
	}
}
