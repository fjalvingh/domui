package db.annotationprocessing;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.beans.Introspector;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Generates QField classes for every Entity annotated class in the project where this processor is selected.
 * Leave the default .apt_generated folder as is.
 *
 * With great gratitude to:
 * <ul>
 *     <li>http://hannesdorfmann.com/annotation-processing/annotationprocessing101</li>
 * </ul>
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 3, 2013
 */
@SupportedAnnotationTypes({"javax.persistence.Entity", "to.etc.annotations.GenerateProperties"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PropertyAnnotationProcessor extends AbstractProcessor {
	static public final String PERSISTENCE_ANNOTATION = "javax.persistence.Entity";

	static public final String GENERATED_PROPERTIES_ANNOTATION = "to.etc.annotations.GenerateProperties";

	static public final String VERSION = "1.0";

	private Types m_typeUtils;

	private Elements m_elementUtils;

	private Messager m_messager;

	private SourceVersion m_sourceVersion;

	private boolean m_debug = System.getenv().get("DOMUI_ANNDEBUG") != null;

	static public final class Property {
		private final TypeMirror m_type;

		private final String m_name;

		private final Set<String> m_annotationNames;

		public Property(TypeMirror type, String name, Set<String> annotationNames) {
			m_type = type;
			m_name = name;
			m_annotationNames = annotationNames;
		}

		public TypeMirror getType() {
			return m_type;
		}

		public String getName() {
			return m_name;
		}

		public Set<String> getAnnotationNames() {
			return m_annotationNames;
		}
	}

	public PropertyAnnotationProcessor() {
		super();
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		m_typeUtils = processingEnv.getTypeUtils();
		m_elementUtils = processingEnv.getElementUtils();
		m_messager = processingEnv.getMessager();
		m_sourceVersion = processingEnv.getSourceVersion();
	}

	public Types getTypeUtils() {
		return m_typeUtils;
	}

	public Elements getElementUtils() {
		return m_elementUtils;
	}

	public Messager getMessager() {
		return m_messager;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(roundEnv.processingOver()) {
			return false;
		}

		final Messager messager = processingEnv.getMessager();
		Set<Element> done = new HashSet<>();
		for(TypeElement ann : annotations) {
			Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(ann);
			for(Element classElement : rootElements) {
				if(!done.add(classElement))
					continue;

				String pkgName = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
				String entityName = classElement.getSimpleName().toString();
				if(m_debug)
					System.out.println("ANN> Processing entity " + entityName);

				try {
					List<Property> properties = getProperties(classElement);

					//generateRootClass(pkgName, entityName, ann, properties);
					generateLinkClass(pkgName, entityName, classElement, ann, properties);
					generateStaticClass(pkgName, entityName, classElement, ann, properties);
				} catch(Exception e1) {

					/*
					 * ecj apparently calls this several times, causing:
					 * Error:(27, 20) java: javax.annotation.processing.FilerException: Source file already exists : nl.skarp.portal.pages.definitions.usability.PiResult_Link in class db.annotationprocessing.PropertyAnnotationProcessor
					 */
					if(!e1.getMessage().toLowerCase().contains("source file already exists")) {
						e1.printStackTrace();
						messager.printMessage(Kind.ERROR, e1.toString() + " in " + getClass(), classElement);
					}
				}
			}
		}
		return false;
	}

	String getStaticClass(String entityName) {
		return entityName + "_";                        // Artist becomes Artist_
	}

	String getLinkClass(String entityName) {
		return entityName + "_Link";
	}

	private JavaFileObject createFile(String name, Element classElement, TypeElement ann) throws IOException {
		if((m_debug))
			System.out.println("ANN> createFile " + name + " source " + ann);
		return processingEnv.getFiler().createSourceFile(name, classElement, ann);
	}

	private void generateStaticClass(String pkgName, String targetClassName, Element classElement, TypeElement ann, List<Property> properties) throws Exception {
		String className = getStaticClass(targetClassName);
		FileObject jf2 = createFile(pkgName + "." + className, classElement, ann);

		try(Writer w = jf2.openWriter()) {
			new StaticClassGenerator(this, w, pkgName, className, properties, targetClassName).generate();
		}
	}

	private void generateLinkClass(String pkgName, String targetClassName, Element classElement, TypeElement ann, List<Property> properties) throws Exception {
		String className = getLinkClass(targetClassName);
		FileObject jf2 = createFile(pkgName + "." + className, classElement, ann);

		try(Writer w = jf2.openWriter()) {
			new LinkClassGenerator(this, w, pkgName, className, properties, targetClassName).generate();
		}
	}

	private List<Property> getProperties(Element classElement) throws Exception {
		Element ce = classElement;

		Map<String, Property> map = new TreeMap<>();
		while(ce != null && !ce.toString().equals("java.lang.Object")) {
			scanProperties(map, ce);

			TypeElement asType = (TypeElement) ce;
			for(TypeMirror ifa : asType.getInterfaces()) {
				if(ifa instanceof DeclaredType) {
					DeclaredType dty = (DeclaredType) ifa;

					scanProperties(map, dty.asElement());
				}
			}

			TypeMirror superclass = asType.getSuperclass();
			if(superclass instanceof DeclaredType) {
				DeclaredType sup = (DeclaredType) superclass;
				ce = sup.asElement();
			} else {
				break;
			}
		}

		List<Property> result = new ArrayList<>(map.values());

		Map<String, List<Property>> byNameMap = result.stream()
			.collect(Collectors.groupingBy(a -> a.getName(), Collectors.toList()));
		return byNameMap.values().stream()
			.map(a -> a.get(0))
			.collect(Collectors.toList());
	}

	private void scanProperties(Map<String, Property> map, Element ce) {
		PropertyVisitor v = new PropertyVisitor();
		ce.accept(v, null);
		for(Property property : v.getResult()) {
			Property rp = map.get(property.getName());
			if(null == rp) {
				map.put(property.getName(), property);
			}
		}
	}



	private final class PropertyVisitor extends ElementScanner6 {
		private final List<Property> m_result = new ArrayList<>();

		private PropertyVisitor() {
		}

		public List<Property> getResult() {
			return m_result;
		}

		/**
		 * Visits a single method.
		 */
		@Override
		public Object visitExecutable(ExecutableElement m, Object p) {
			//-- We accept only the getters, isxxx and getxxxx
			String methodName = m.getSimpleName().toString();
			String propertyName;
			if(methodName.startsWith("is")) {
				propertyName = Introspector.decapitalize(methodName.substring(2));
			} else if(methodName.startsWith("get")) {
				propertyName = Introspector.decapitalize(methodName.substring(3));
			} else {
				return super.visitExecutable(m, p);
			}

			TypeMirror returnType = m.getReturnType();
			if(returnType instanceof javax.lang.model.type.NoType) {    // void?
				return super.visitExecutable(m, p);
			}

			//-- Get a set of annotation names
			Set<String> annotationNames = new HashSet<>();
			for(AnnotationMirror a : m.getAnnotationMirrors()) {
				Name annName = a.getAnnotationType().asElement().getSimpleName();
				annotationNames.add(annName.toString());
				//System.out.println(">>>> ann name " + annName);
			}
			if(annotationNames.contains("IgnoreGeneration")) {    // Ignore?
				return super.visitExecutable(m, p);
			}

			//-- It cannot have arguments
			if(!m.getParameters().isEmpty()) {
				return super.visitExecutable(m, p);
			}

			if(m_debug)
				System.out.println("property " + propertyName + " type " + returnType);
			m_result.add(new Property(returnType, propertyName, annotationNames));
			return super.visitExecutable(m, p);
		}
	}

	public SourceVersion getSourceVersion() {
		return m_sourceVersion;
	}

	/**
	 * Pretty dumb to use an enum to represent versions if you want to be backward compatible, so
	 * convert the fuckup into a number.
	 */
	public int getUnfsckedVersionNumber() {
		String ver = System.getProperty("java.specification.version");
		if(ver == null)
			return 8;
		try {
			return Integer.parseInt(ver);
		} catch(Exception x) {
			return 8;
		}
	}

}
