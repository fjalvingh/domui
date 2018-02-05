package to.etc.domui.component.lookup.filter;

import java.util.*;
import java.util.Map.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.webapp.query.*;
import to.etc.xml.*;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/8/16.
 */
@DefaultNonNull
public final class LookupFilterTranslatorRegistry {

	private static Map<String, ITranslator<?>> m_translators = new HashMap<>();

	static {
		LookupFilterTranslatorRegistry.register(new StringTranslator());
		LookupFilterTranslatorRegistry.register(new BooleanTranslator());
		LookupFilterTranslatorRegistry.register(new IntegerTranslator());
		LookupFilterTranslatorRegistry.register(new BigIntegerTranslator());
		LookupFilterTranslatorRegistry.register(new IIdentifyableTranslator());
		LookupFilterTranslatorRegistry.register(new DateFromToTranslator());
		LookupFilterTranslatorRegistry.register(new SetTranslator());
		LookupFilterTranslatorRegistry.register(new EnumTranslator<>());
	}

	public static synchronized void register(ITranslator<?> translator) {
		m_translators = new HashMap<>(m_translators);
		m_translators.put(translator.getClass().getCanonicalName(), translator);
	}

	public static void serialize(XmlWriter writer, Object o, @Nullable String key) throws Exception {
		for(Entry<String, ITranslator<?>> entry : m_translators.entrySet()) {
			ITranslator<?> translator = entry.getValue();
			if(translator.serialize(writer, o)) {
				writer.tagonly(ITranslator.METADATA, ITranslator.KEY, key, ITranslator.TYPE, translator.getClass().getCanonicalName());
				return;
			}
		}
		throw new IllegalArgumentException("I could not serialize this key/object: " + key + "/" + o);// FIXME ignore the error until this fix works completely
	}

	@Nullable
	public static Object deserialize(QDataContext dc, String serializerType, Node node) throws Exception {
		ITranslator<?> translator = m_translators.get(serializerType);
		if(translator == null) {
			return null;
		}
		return translator.deserialize(dc, node);
	}
}
