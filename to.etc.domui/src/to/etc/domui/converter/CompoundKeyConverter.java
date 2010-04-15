package to.etc.domui.converter;

import java.math.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

/**
 * URL Converter class which converts a (compound) primary key into a string and v.v.
 * The converter will follow object references on each (embedded) object in the key until
 * it reaches a renderable value for a property; these are added by walking the object tree
 * by following all properties in alphabetic order.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2010
 */
public class CompoundKeyConverter implements IConverter<Object> {
	static public final CompoundKeyConverter INSTANCE = new CompoundKeyConverter();

	@Override
	public String convertObjectToString(Locale loc, Object in) throws UIException {
		if(in == null)
			return "$$null$$";
		StringBuilder sb = new StringBuilder();
		//		StringBuilder namesb = new StringBuilder();
		try {
			renderAnything(sb, in, in);
		} catch(Exception x) {
			if(x instanceof UIException)
				throw (UIException) x;
			throw WrappedException.wrap(x);
		}
		return sb.toString();
	}

	private void renderRenderable(StringBuilder sb, Object pvalue) throws Exception {
		if(sb.length() != 0)
			sb.append(",");
		if(pvalue instanceof String)
			sb.append(((String) pvalue).replace(",", "\\,").replace("\\", "\\\\"));
		else
			sb.append(String.valueOf(pvalue));
	}

	private void renderAnything(StringBuilder sb, Object in, Object root) throws Exception {
		if(in == null)
			throw new IllegalStateException("Unexpected: null value in PK " + root + " of class " + root.getClass());
		Class< ? > clz = in.getClass();

		//-- Primitives are rendered immediately and exited.
		if(isRenderable(clz)) {
			renderRenderable(sb, in);
			return;
		}

		//-- Depending on the type of object.....
		ClassMetaModel cmm = MetaManager.findClassMeta(in.getClass());
		if(cmm.isPersistentClass()) {
			renderPersistentClass(sb, in, root, cmm);
			return;
		}

		//-- Seems to be some other object... Render it.
		renderObject(sb, in, root, cmm);
	}

	private void renderPersistentClass(StringBuilder sb, Object in, Object root, ClassMetaModel cmm) throws Exception {
		//-- Not renderable: only acceptable item is another persistent class, in which case we need to render it's PK too
		if(!cmm.isPersistentClass())
			throw new IllegalStateException("Unexpected: PK entry is not a persistent class: " + cmm + ", in root PK " + root.getClass());

		//-- Obtain it's PK and render it as well
		PropertyMetaModel pkpm = cmm.getPrimaryKey();
		if(pkpm == null)
			throw new IllegalStateException("Unexpected: persistent class " + cmm + " has an undefined PK property");
		Object pkval = pkpm.getAccessor().getValue(in);
		renderAnything(sb, pkval, root);
	}

	private void renderObject(StringBuilder sb, Object in, Object root, ClassMetaModel cmm) throws Exception {
		for(PropertyMetaModel pmm : cmm.getProperties()) {
			Object pvalue = pmm.getAccessor().getValue(in);
			renderAnything(sb, pvalue, root);
		}
	}

	/**
	 * Returns T if the type can be rendered as a PK component.
	 * @param actualType
	 * @return
	 */
	private boolean isRenderable(Class< ? > t) {
		if(t.isPrimitive())
			return true;
		if(t == Integer.class || t == Long.class || t == Short.class || t == String.class || t == Byte.class || t == BigDecimal.class || t == BigInteger.class || t == Double.class || t == Float.class
			|| t == Date.class)
			return true;
		return false;
	}

	@Override
	public Object convertStringToObject(Locale loc, String in) throws UIException {
		return null;
	}
}
