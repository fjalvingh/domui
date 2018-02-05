package to.etc.domui.component.lookup.filter;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.domui.component.meta.*;
import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 * Translates an Enum value to XML and vice versa
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@DefaultNonNull
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
