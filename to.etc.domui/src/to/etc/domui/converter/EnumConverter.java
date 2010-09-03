package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This generic converter for enums should be used only as convertObjectToString renderer.
 * It is used for default rendering of enum fields inside table results.
 * Backward conversion using  convertStringToObject is not supported and would throw exception.
 *
 * @author vmijic
 * Created on 29 Jul 2009
 */
public class EnumConverter<E extends Enum<E>> implements IConverter<E> {

	@Override
	public String convertObjectToString(Locale loc, E in) throws UIException {
		if(in == null)
			return "";
		ClassMetaModel ecmm = MetaManager.findClassMeta(in.getClass());
		return ecmm.getDomainLabel(NlsContext.getLocale(), in);
	}

	@Override
	public E convertStringToObject(Locale loc, String input) throws UIException {
		throw new ValidationException(Msgs.UNEXPECTED_EXCEPTION);
	}

}
