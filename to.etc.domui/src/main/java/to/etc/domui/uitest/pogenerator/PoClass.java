package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

	private final List<Pair<String, String>> m_interfaceList;

	private final List<PoField> m_fieldList = new ArrayList<>();

	private final List<PoMethod> m_methodList = new ArrayList<>();

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

	public PoClass(String packageName, String className, @Nullable PoClass baseClass, List<Pair<String, String>> interfaceList) {
		m_packageName = packageName;
		m_className = className;
		m_baseClass = baseClass;
		m_interfaceList = interfaceList;
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

	public PoField addField(String typePackage, String typeName, String fieldName) {
		PoField field = new PoField(this, typePackage, typeName, fieldName);
		add(field);
		return field;
	}

	public PoField addField(Pair<String, String> type, String fieldName) {
		return addField(type.get1(), type.get2(), fieldName);
	}

	public PoClass addField(String fullType, String fieldName) {
		int ix = fullType.lastIndexOf('.');
		if(ix == -1) {
			add(new PoField(this, "", fullType, fieldName));
		} else {
			String packageName = fullType.substring(0, ix);
			String typeName = fullType.substring(ix + 1);
			addImport(packageName, typeName);
			add(new PoField(this, packageName, typeName, fieldName));
		}
		return this;
	}

	/**
	 * Add a method. The method is returned so that it can be further configured.
	 */
	public PoMethod addMethod(@Nullable Pair<String, String> returnType, String name, Modifier... modifiers) {
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

	public List<Pair<String, String>> getInterfaceList() {
		return m_interfaceList;
	}

	public List<PoField> getFieldList() {
		return m_fieldList;
	}

	public List<PoMethod> getMethodList() {
		return m_methodList;
	}

	public PoClass addImport(Pair<String, String> type) {
		addImport(type.get1(), type.get2());
		return this;
	}

	public PoClass addImport(String packageName, String className) {
		if(packageName.length() == 0)
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

	public boolean isMarkGenerated() {
		return m_markGenerated;
	}

	public boolean hasImport(String fullName) {
		return m_importSet.contains(fullName);
	}
}
