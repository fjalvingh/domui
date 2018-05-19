package db.annotationprocessing;

import db.annotationprocessing.PropertyAnnotationProcessor.Property;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-3-18.
 */
public class LinkClassGenerator extends ClassGenerator {
	public LinkClassGenerator(PropertyAnnotationProcessor propertyAnnotationProcessor, Writer w, String packageName, String className,
		List<Property> properties, String entityName) {
		super(propertyAnnotationProcessor, w, packageName, className, properties, entityName);
	}

	@Override protected void generateClassExtends() throws IOException {
		m_w.append("<R> extends QField<R, ");
		m_w.append(getTargetClassName());
		m_w.append(">");
	}

	@Override protected void generateConstructor() throws IOException {
		//super.generateConstructor();							// Generate the empty contructor

		append("\tpublic ").append(getClassName()).append("(@NonNull Class<R> rootClass, @NonNull String propertyName) {\n");
		append("\t\tsuper(rootClass, propertyName);\n");
		append("\t}\n\n");

		append("\tpublic ").append(getClassName()).append("(@NonNull Class<R> rootClass, @Nullable QField<R,?> parent, @NonNull String propertyName) {\n");
		append("\t\tsuper(rootClass, parent, propertyName);\n");
		append("\t}\n\n");
	}

	@Override
	protected void generateColumnProperty(TypeMirror returnType, String propertyName) throws Exception {
		String mname = replaceReserved(propertyName);
		String mtypeName = getWrappedType(returnType.toString());

		m_w.append("\t@NonNull\n\tpublic final QField<R,");
		m_w.append(mtypeName);
		m_w.append("> ");
		m_w.append(mname);
		m_w.append("() {\n\t\treturn new QField<R,");
		m_w.append(mtypeName);
		m_w.append(">(getRootClass(), this, \"").append(propertyName).append("\");\n");
		m_w.append("\t}\n\n");
	}

	@Override protected void generateCollectionProperty(TypeMirror type, String name) throws Exception {
		generateColumnProperty(type, name);
	}

	@Override
	protected void generateParentProperty(TypeMirror returnType, String propertyName) throws Exception {
		System.out.println("ANN: parent type is " + returnType);
		Element mtype = typeUtils().asElement(returnType);
		String qtype;
		if(null == mtype) {
			//-- non-source class?
			qtype = packName(returnType.toString()) + "." + m_processor.getLinkClass(getSimpleName(returnType.toString()));
		} else {
			qtype = packName(returnType.toString()) + "." + m_processor.getLinkClass(mtype.getSimpleName().toString());
		}
		String mname = replaceReserved(propertyName);
		m_w.append("\t@NonNull\n");
		m_w.append("\tpublic final ").append(qtype).append("<R> ").append(mname).append("() {");
		m_w.append("\n\t\treturn new ").append(qtype).append("<R>(getRootClass(), this, \"").append(propertyName).append("\");\n");
		m_w.append("\t}\n\n");
	}
}
