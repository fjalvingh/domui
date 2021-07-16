package to.etc.domui.webdriver.core.pogeneration

import org.eclipse.jdt.annotation.NonNullByDefault
import to.etc.domui.webdriver.core.WebDriverConnector
import to.etc.domui.webdriver.core.base.BasePO
import to.etc.util.StringTool
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.util.stream.Collectors

class JavaPageObjectPrinter(genClassModel: GeneratedClassModel) : PageObjectPrinter {
	private val sb = StringBuilder()
	private val m_actual: GeneratedClassModel
	private val imports = HashSet<String>();

	init {
		m_actual = GeneratedClassModel(genClassModel.name, genClassModel.namespace, ArrayList(genClassModel.members), ArrayList(genClassModel.methods))
		val toRemove = ArrayList<GeneratedClassMember>()
		genClassModel.members.forEach{
			if(it.accessModifier == POAccessModifier.PRIVATE) {
				m_actual.members.add(GeneratedClassMember("m_${it.name}", it.type, it.accessModifier, it.params))
				toRemove.add(it)
				m_actual.methods.add(GeneratedClassMethod("get${StringTool.strCapitalizedIntact(it.name)}", it.type, POAccessModifier.PUBLIC, "return m_${clean(it.name)};"))
			}
		}
		m_actual.members.removeAll(toRemove)
	}

	override fun print(): String {
		appendPackage()
		appendImports()
		sb.append("\n")
		appendClassDeclaration()
		sb.append("\n")
		appendMembers()
		sb.append("\n")
		appendConstructor()
		sb.append("\n")
		appendMethods()
		sb.append("}\n")

		return sb.toString()
	}

	private fun actualClassName(): String {
		return "${m_actual.name}PO"
	}

	private fun appendConstructor() {
		sb.append("\tpublic ${actualClassName()}(WebDriverConnector wd) { \n \tsuper(wd); \n\t }")
	}

	private fun appendPackage() {
		sb.append("package ${m_actual.namespace};\n\n");
	}

	private fun appendImports() {
		imports.add(BasePO::class.java.name)
		imports.add(WebDriverConnector::class.java.name)
		imports.add(NonNullByDefault::class.java.name)
		m_actual.members.forEach{
			val n = it.type.name;
			if(!n.startsWith("java.lang")) {
				imports.add(n)
			}
		}
		m_actual.methods.forEach {
			val n = it.type?.name;
			if(n != null && !n.startsWith("java.lang")) {
				imports.add(n)
			}
			it.parameters.forEach{ param->
				val na = param.type.name;
				if(na != null && !na.startsWith("java.lang")) {
					imports.add(na)
				}
			}
		}
		imports.forEach{
			sb.append("import $it;\n")
		}
		sb.append("\n")
	}

	private fun clean(str: String): String {
		return str
			.replace(" ", "_")
			.replace("-", "_")
			.replace("=", "")
			.replace("(", "")
			.replace(")", "")
			.replace("*", "")
			.replace(";", "")
			.replace("/", "")
			.replace("\\", "");
	}

	private fun appendClassDeclaration() {
		sb.append("@NonNullByDefault() \n")
		sb.append("public class ${actualClassName()} extends BasePO {\n")
	}

	private fun appendMembers() {
		m_actual.members.forEach{
			appendMember(it)
		}
	}

	private fun tab() {
		sb.append("\t")
	}

	private fun appendMember(it: GeneratedClassMember) {
		tab()
		appendAccessModifier(it.accessModifier)
		sb.append("${it.type.simpleName} ${clean(it.name)}")
		if(!it.params.isEmpty()) {
			sb.append(" = new ${it.type.simpleName}(")
			sb.append(it.params.stream().collect(Collectors.joining(", ")))
			sb.append(")")
		}
		sb.append(";\n")
	}

	private fun appendAccessModifier(it: POAccessModifier) {
		when(it) {
			POAccessModifier.PUBLIC -> sb.append("public ")
			POAccessModifier.PRIVATE -> sb.append("private ")
			POAccessModifier.PROTECTED -> sb.append("protected ")
			POAccessModifier.INTERNAL -> sb.append("")
			else -> throw IllegalStateException("Unsupported access modifier ${it}")
		}
	}

	private fun appendMethods() {
		m_actual.methods.forEach{
			appendMethod(it)
		}
	}

	private fun appendMethod(it: GeneratedClassMethod) {
		tab()
		appendAccessModifier(it.accessModifier)
		if(it.type == null) {
			sb.append("void")
		}else {
			sb.append(it.type.simpleName)
		}
		sb.append(" ${clean(it.name)}(")
		sb.append(it.parameters.stream().map{x-> "${x.type.simpleName} ${x.name}" } .collect(Collectors.joining(",")))
		sb.append(") { \n")
		tab()
		tab()
		sb.append(it.body)
		sb.append("\n\t}\n")
	}
}
