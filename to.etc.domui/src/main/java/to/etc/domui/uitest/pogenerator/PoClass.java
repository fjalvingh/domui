package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Represents a class to be generated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoClass {
	private final String m_packageName;

	private final String m_className;

	@Nullable
	private final PoClass m_baseClass;

	private final List<RefType> m_interfaceList;

	private final List<PoField> m_fieldList = new ArrayList<>();

	private final List<PoMethod> m_methodList = new ArrayList<>();

	private final List<PoMethod> m_constructorList = new ArrayList<>();

	/**
	 * Full imports required.
	 */
	private Set<String> m_importSet = new TreeSet<>();				// Keep 'm sorted

	/**
	 * Imports by name. If we have a dup here the second added will be used as a full name reference.
	 */
	private Set<String> m_singleNameImport = new TreeSet<>();

	private final Set<String> m_baseNamesUsedSet = new HashSet<>();

	private boolean m_markGenerated;

	private final List<RefType> m_genericParameterList = new ArrayList<>();

	public PoClass(String packageName, String className, @Nullable PoClass baseClass, List<RefType> interfaceList) {
		m_packageName = packageName;
		m_className = className;
		m_baseClass = baseClass;
		m_interfaceList = interfaceList;
	}

	public PoClass(String packageName, String className) {
		m_packageName = packageName;
		m_className = className;
		m_baseClass = null;
		m_interfaceList = Collections.emptyList();
	}

	public PoClass(String packageName, String className, @Nullable PoClass baseClass) {
		m_packageName = packageName;
		m_className = className;
		m_baseClass = baseClass;
		m_interfaceList = Collections.emptyList();
	}

	public void addGenericParameter(RefType clz) {
		m_genericParameterList.add(clz);
	}

	public String getBaseName(NodeBase node) {
		String testID = node.getTestID();
		if(null == testID)
			throw new IllegalStateException("Node " + node + " does not have a testID");
		return getBaseName(testID);
	}

	public String getBaseName(String testId) {
		String baseName = PoGeneratorContext.removeUnderscores(PoGeneratorContext.clean(testId));
		String tryName = baseName;
		for(int i = 0; i < 10; i++) {
			if(m_baseNamesUsedSet.add(tryName))
				return tryName;
			tryName = baseName + i;
		}
		throw new IllegalStateException("Out of names to try for " + testId);
	}

	public PoClass generated() {
		m_markGenerated = true;
		return this;
	}

	public PoClass add(PoMethod method) {
		m_methodList.add(method);
		return this;
	}

	public PoClass add(PoField field) {
		m_fieldList.add(field);
		return this;
	}

	public PoField addField(String packageName, String typeName, String fieldName) {
		RefType rt = new RefType(packageName, typeName);
		PoField field = new PoField(this, rt, fieldName);
		add(field);
		return field;
	}

	public PoField addField(RefType type, String fieldName) {
		PoField field = new PoField(this, type, fieldName);
		add(field);
		return field;
	}

	public PoClass addField(String fullType, String fieldName) {
		int ix = fullType.lastIndexOf('.');
		if(ix == -1) {
			RefType rt = new RefType("", fullType);
			add(new PoField(this, rt, fieldName));
		} else {
			String packageName = fullType.substring(0, ix);
			String typeName = fullType.substring(ix + 1);
			RefType rt = new RefType(packageName, typeName);
			addImport(packageName, typeName);
			add(new PoField(this, rt, fieldName));
		}
		return this;
	}

	/**
	 * Add a method. The method is returned so that it can be further configured.
	 */
	public PoMethod addMethod(@Nullable RefType returnType, String name, Modifier... modifiers) {
		PoMethod m = new PoMethod(this, returnType, name, modifiers);
		m_methodList.add(m);
		return m;
	}

	public void visit(IPoModelVisitor v) throws Exception {
		v.visitClass(this);
	}

	public String getPackageName() {
		return m_packageName;
	}

	public String getClassName() {
		return m_className;
	}

	@Nullable
	public PoClass getBaseClass() {
		return m_baseClass;
	}

	public List<RefType> getInterfaceList() {
		return m_interfaceList;
	}

	public List<PoField> getFieldList() {
		return m_fieldList;
	}

	public List<PoMethod> getMethodList() {
		return m_methodList;
	}

	public PoClass addImport(RefType type) {
		addImport(type.getPackageName(), type.getTypeName());
		return this;
	}

	public PoClass addImport(String packageName, String className) {
		if(packageName.length() == 0 || packageName.startsWith("java.lang."))
			return this;
		if(packageName.equals(m_packageName)) {					// Same package as class -> just add as named
			m_singleNameImport.add(className);
			return this;
		}

		String fullName = packageName + "." + className;
		if(m_importSet.contains(fullName))						// Already there?
			return this;

		// Before we add: make sure the result is unique by name
		if(m_singleNameImport.add(className)) {
			m_importSet.add(fullName);
		}
		return this;
	}

	public Set<String> getImportSet() {
		return m_importSet;
	}

	public List<RefType> getGenericParameterList() {
		return m_genericParameterList;
	}

	public List<PoMethod> getConstructorList() {
		return m_constructorList;
	}

	public boolean isMarkGenerated() {
		return m_markGenerated;
	}

	public boolean hasImport(String fullName) {
		if(fullName.startsWith("java.lang."))
			return true;
		return m_importSet.contains(fullName);
	}

	public RefType asType() {
		List<String> plist = getGenericParameterList().stream()
			.map(a -> a.asTypeString())
			.collect(Collectors.toList());

		return new RefType(getPackageName(), getClassName(), plist);
	}

	public PoMethod addConstructor() {
		PoMethod m = new PoMethod(this, null, getClassName(), Modifier.Public);
		m_constructorList.add(m);
		return m;
	}
}
