package to.etc.iocular.def;

import to.etc.iocular.*;

/**
 * INTERNAL INTERFACE
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 25, 2009
 */
public interface ISelfDef {
	String getIdent();

	BindingScope getScope();

	Class< ? >[] getDefinedTypes();

	String[] getNames();

	String getDefinitionLocation();

	Class< ? > getActualClass();
}
