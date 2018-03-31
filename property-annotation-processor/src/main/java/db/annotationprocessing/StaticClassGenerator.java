package db.annotationprocessing;

import db.annotationprocessing.EntityAnnotationProcessor.Property;

import javax.lang.model.element.Element;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.io.Writer;
import java.util.List;

/**
 * This generates the xxx_ class which forms the "ui" class for the tree. It consists of all
 * static methods returning the root of the path for all properties for the target class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-3-18.
 */
public class StaticClassGenerator extends ClassGenerator {
	public StaticClassGenerator(EntityAnnotationProcessor entityAnnotationProcessor, Writer w, String packageName, String className,
		List<Property> properties, String entityName) {
		super(entityAnnotationProcessor, w, packageName, className, properties, entityName);
	}

	@Override
	protected void generateColumnProperty(TypeMirror returnType, String propertyName) throws Exception {
		String mname = replaceReserved(propertyName);
		String linkClass = getRootClassName();

		if(false && returnType instanceof PrimitiveType) {
			String retStr = returnType.toString();
			if(retStr.equals("int") || retStr.equals("short")) {
				retStr = "long";
			}
			String mtypeName = Character.toUpperCase(retStr.charAt(0)) + retStr.substring(1);
			m_w.append("\n\n\t@Nonnull\n\tstatic public final QField<");
			m_w.append(linkClass);
			m_w.append(", ").append(mtypeName).append("> ");
			m_w.append(mname);
			m_w.append("() {\n\t\treturn new QField");
			m_w.append(mtypeName);
			m_w.append("<").append(linkClass).append(">");
			m_w.append("(new QField<").append(linkClass).append(", ");
			m_w.append(retStr);
			m_w.append("[]>();\n");
			m_w.append("\t}\n");

		} else {
			String mtypeName = getWrappedType(returnType.toString());

			m_w.append("\n\n\t@Nonnull\n\tstatic public final QField<");
			m_w.append(linkClass);
			m_w.append(", ").append(mtypeName).append("> ");
			m_w.append(mname);
			m_w.append("() {\n\t\treturn new QField<").append(linkClass).append(", ");
			m_w.append(mtypeName);
			m_w.append(">();\n");
			m_w.append("\t}\n");
		}
	}

	@Override
	protected void generateParentProperty(TypeMirror returnType, String propertyName) throws Exception {
		Element mtype = typeUtils().asElement(returnType);
		String qtype = packName(returnType.toString()) + "." + m_processor.getLinkClass(mtype.getSimpleName().toString());
		String linkClass = getRootClassName();

		String mname = replaceReserved(propertyName);
		m_w.append("\n\n\t@Nonnull\n\tpublic final ");
		m_w.append(qtype);
		m_w.append("<").append(linkClass).append("> ");
		m_w.append(mname);
		m_w.append("() {\n\t\treturn new ");
		m_w.append(qtype);
		m_w.append("<").append(getRootClassName()).append(">(null, \"");
		m_w.append(propertyName);
		m_w.append("\");\n\t}");
	}
}
