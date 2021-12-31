package to.etc.domui.server;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.injector.IInjectedPropertyAccessChecker;
import to.etc.util.ClassUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-12-21.
 */
public class InjectorRightsChecker {
	static private final Logger LOG = LoggerFactory.getLogger(InjectorRightsChecker.class);

	private final DomApplication m_application;

	private int m_errorCount;

	private Set<Class<?>> m_uncheckedSet = new HashSet<>();

	private Set<Class<?>> m_checkedSet = new HashSet<>();

	public InjectorRightsChecker(DomApplication application) {
		m_application = application;
	}

	public void scan() {
		List<IInjectedPropertyAccessChecker> checkerList = m_application.getInjectedPropertyAccessCheckerList();
		if(checkerList.size() == 0) {
			m_errorCount++;
			LOG.error("No injected value right checkers have been registered, skipping page check.");
			LOG.error("THIS IS A SECURITY RISK; HACKERS CAN GUESS PRIMARY KEYS AND MIGHT ACCESS DATA WITHOUT AUTHORIZATION");
			return;
		}

		ScanResult r = m_application.getClasspathScanResult();
		int pages = 0;
		for(ClassInfo classInfo : r.getAllClasses()) {
			if(isPageClass(classInfo)) {
				pages++;
				scanPageInjectors(classInfo);
			}
		}

		if(getErrorCount() == 0) {
			LOG.info("[ic] Found " + pages + " pages with " + m_errorCount + " errors and " + m_uncheckedSet.size() + " unchecked data classes");
		} else {
			LOG.error("[ic] Found " + pages + " pages with " + m_errorCount + " errors and " + m_uncheckedSet.size() + " unchecked data classes");
			for(Class<?> aClass : m_uncheckedSet) {
				LOG.error("[ic] Unchecked injected data type: " + aClass.getName());
			}
		}
	}

	/**
	 * Find all {@link to.etc.domui.annotations.UIUrlParameter} annotated methods.
	 */
	private void scanPageInjectors(ClassInfo classInfo) {
		for(MethodInfo methodInfo : classInfo.getMethodInfo()) {
			if(methodInfo.hasAnnotation(UIUrlParameter.class.getName())) {
				checkMethodInjector(classInfo, methodInfo);
			}
		}
	}

	private void checkMethodInjector(ClassInfo classInfo, MethodInfo methodInfo) {
		TypeSignature resultType = methodInfo.getTypeDescriptor().getResultType();
		if(null == resultType)
			return;
		String s = resultType.toString();
		if(s.startsWith("java.") || ! s.contains("."))							// Do not check basic types
			return;

		String clzName = classInfo.getName();
		//String propName = methodInfo.getName();
		//if(propName.startsWith("is"))
		//	propName = propName.substring(2);
		//else if(propName.sta)

		Class<?> parameterClass = ClassUtil.loadClass(getClass().getClassLoader(), s);
		if(null == parameterClass) {
			m_errorCount++;
			error(clzName + "." + methodInfo.getName() + ": cannot load " + s);
			return;
		}

		List<IInjectedPropertyAccessChecker> checkerList = m_application.getInjectedPropertyAccessCheckerList();

		for(IInjectedPropertyAccessChecker checker : checkerList) {
			if(checker.checks(parameterClass)) {
				m_checkedSet.add(parameterClass);
				return;
			}
		}
		m_uncheckedSet.add(parameterClass);
		error(clzName + "." + methodInfo.getName() + ": Parameter type " + s + " is not checked");
	}

	private void error(String s) {
		LOG.error("[ic] " + s);
	}

	public int getErrorCount() {
		return m_errorCount + m_uncheckedSet.size();
	}

	public Set<Class<?>> getUncheckedSet() {
		return m_uncheckedSet;
	}

	public Set<Class<?>> getCheckedSet() {
		return m_checkedSet;
	}

	private boolean isPageClass(ClassInfo classInfo) {
		return classInfo.extendsSuperclass(UrlPage.class.getName());
	}
}
