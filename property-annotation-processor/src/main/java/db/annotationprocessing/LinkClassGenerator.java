package db.annotationprocessing;

import db.annotationprocessing.EntityAnnotationProcessor.Property;

import javax.lang.model.element.Element;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-3-18.
 */
public class LinkClassGenerator extends ClassGenerator {
	public LinkClassGenerator(EntityAnnotationProcessor entityAnnotationProcessor, Writer w, String packageName, String className,
		List<Property> properties, String entityName) {
		super(entityAnnotationProcessor, w, packageName, className, properties, entityName);
	}

	@Override protected void generateClassExtends() throws IOException {
		m_w.append("<R extends QField<R, ? >> extends QField<R, ");
		m_w.append(getLinkClass());
		m_w.append(">");
	}

	@Override protected void generateConstructor() throws IOException {
		super.generateConstructor();							// Generate the empty contructor

		append("\t").append(getClassName()).append("(@Nullable QField<R,?> parent, @Nonnull String parentProperty) {\n");
		append("\t\tsuper(parent, parentProperty);\n");
		append("\t}\n");
	}

	@Override
	protected void generateColumnProperty(TypeMirror returnType, String propertyName) throws Exception {
		String mname = replaceReserved(propertyName);
		if(false && returnType instanceof PrimitiveType) {
			String retStr = returnType.toString();
			if(retStr.equals("int") || retStr.equals("short")) {
				retStr = "long";
			}
			String mtypeName = Character.toUpperCase(retStr.charAt(0)) + retStr.substring(1);
			m_w.append("\n\n\t@Nonnull\n\tpublic final QField");
			m_w.append(mtypeName);
			m_w.append("<R> ");
			m_w.append(mname);
			m_w.append("() {\n\t\treturn new QField");
			m_w.append(mtypeName);
			m_w.append("<R>(new QField<R, ");
			m_w.append(retStr);
			m_w.append("[]>(this, \"");
			m_w.append(propertyName);
			m_w.append("\"));\n\t}");

		} else {
			String mtypeName = getWrappedType(returnType.toString());

			m_w.append("\n\n\t@Nonnull\n\tpublic final QField<R,");
			m_w.append(mtypeName);
			m_w.append("> ");
			m_w.append(mname);
			m_w.append("() {\n\t\treturn new QField<R,");
			m_w.append(mtypeName);
			m_w.append(">(this, \"");
			m_w.append(propertyName);
			m_w.append("\");\n\t}");
		}
	}

	@Override
	protected void generateParentProperty(TypeMirror returnType, String propertyName) throws Exception {
		Element mtype = typeUtils().asElement(returnType);
		String qtype = packName(returnType.toString()) + "." + m_processor.getLinkClass(mtype.getSimpleName().toString());

		String mname = replaceReserved(propertyName);
		m_w.append("\n\n\t@Nonnull\n\tpublic final ");
		m_w.append(qtype);
		m_w.append("<R> ");
		m_w.append(mname);
		m_w.append("() {\n\t\treturn new ");
		m_w.append(qtype);
		m_w.append("<R>(this, \"");
		m_w.append(propertyName);
		m_w.append("\");\n\t}");
	}

}
