package db.annotationprocessing;

import db.annotationprocessing.PropertyAnnotationProcessor.Property;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
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
	public StaticClassGenerator(PropertyAnnotationProcessor propertyAnnotationProcessor, Writer w, String packageName, String className,
		List<Property> properties, String entityName) {
		super(propertyAnnotationProcessor, w, packageName, className, properties, entityName);
	}

	@Override protected void generateConstructor() throws IOException {
		append("\tprivate ").append(getStaticClassName()).append("() {\n");
		append("\t}\n\n");
	}

	@Override
	protected void generateColumnProperty(TypeMirror returnType, String propertyName) throws Exception {
		String mname = replaceReserved(propertyName);
		//String linkClass = getRootClassName();

		String mtypeName = getWrappedType(returnType.toString());

		m_w.append("\t@NonNull\n");
		m_w.append("\tstatic public final QField<");
		m_w.append(getTargetClassName());
		m_w.append(", ").append(mtypeName).append("> ");
		m_w.append(mname);
		m_w.append("() {");
		m_w.append("\n\t\treturn new QField<>(").append(getTargetClassName()).append(".class, \"").append(propertyName).append("\");\n");
		m_w.append("\t}\n\n");
	}

	@Override protected void generateCollectionProperty(TypeMirror type, String name) throws Exception {
		generateColumnProperty(type, name);
	}

	@Override
	protected void generateParentProperty(TypeMirror returnType, String propertyName) throws Exception {
		Element mtype = typeUtils().asElement(returnType);
		String qtype;
		if(null == mtype) {
			//-- non-source class?
			qtype = packName(returnType.toString()) + "." + m_processor.getLinkClass(getSimpleName(returnType.toString()));
		} else {
			qtype = packName(returnType.toString()) + "." + m_processor.getLinkClass(mtype.getSimpleName().toString());
		}
		String mname = replaceReserved(propertyName);
		m_w.append("\t@NonNull\n\tstatic public final ");
		m_w.append(qtype);
		m_w.append("<").append(getTargetClassName()).append("> ");
		m_w.append(mname);
		m_w.append("() {\n\t\treturn new ");
		m_w.append(qtype);
		m_w.append("<>(").append(getTargetClassName()).append(".class, null, \"");
		m_w.append(propertyName);
		m_w.append("\");\n");
		m_w.append("\t}\n\n");
	}
}
