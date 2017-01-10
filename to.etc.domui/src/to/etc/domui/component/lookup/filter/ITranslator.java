package to.etc.domui.component.lookup.filter;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.webapp.query.*;
import to.etc.xml.*;

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
	T deserialize(@Nonnull QDataContext dc, @Nonnull Node node) throws Exception;

	boolean serialize(@Nonnull XmlWriter writer, @Nonnull Object o) throws Exception;

}
