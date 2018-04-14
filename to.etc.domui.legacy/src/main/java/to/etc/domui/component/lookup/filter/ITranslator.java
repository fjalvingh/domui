package to.etc.domui.component.lookup.filter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Node;
import to.etc.webapp.query.QDataContext;
import to.etc.xml.XmlWriter;

/**
 * The serialize method writes the characteristics of the object to the XmlWriter
 * The deserialize translates the xml from the Node back to the object
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
public interface ITranslator<T> {

	String KEY = "key";

	String VALUE = "value";

	String TYPE = "type";

	String METADATA = "metaData";

	String ITEM = "item";

	String CLASS = "class";

	@Nullable
	T deserialize(@NonNull QDataContext dc, @NonNull Node node) throws Exception;

	boolean serialize(@NonNull XmlWriter writer, @NonNull Object o) throws Exception;

}
