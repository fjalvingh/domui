package to.etc.domui.webdriver.core.pogeneration

import to.etc.domui.component.input.Text2
import to.etc.domui.dom.html.NodeBase
import to.etc.domui.dom.html.NodeContainer
import to.etc.domui.webdriver.core.pogeneration.POGeneration.generators
import to.etc.domui.component.buttons.DefaultButton
import to.etc.domui.component2.lookupinput.LookupInput2
import to.etc.domui.dom.html.Button
import to.etc.domui.util.DomUtil
import to.etc.domui.util.DomUtil.IPerNode
import java.util.concurrent.ConcurrentHashMap

interface POFactory{
	fun create(s: NodeBase) : POCodeGenerator
}

object POGeneration {
	internal val generators: MutableMap<Class<*>, POFactory> = ConcurrentHashMap()

	fun registerFactory(clazz: Class<*>, factory: POFactory) {
		generators[clazz] = factory
	}

	init {
		registerFactory(Text2::class.java, Text2Factory())
		registerFactory(LookupInput2::class.java, LookupFactory())
		registerFactory(Button::class.java, ButtonFactory())
		registerFactory(DefaultButton::class.java, ButtonFactory())
		registerFactory(SuitableNodeSelectionFragment::class.java, SkipPOFactory())
	}
}

class POGenerator(val page: NodeContainer) {
	val suitableNodes = ArrayList<NodeBase>()
	fun findAllSuitable(): List<NodeBase> {
		suitableNodes.clear()
		DomUtil.walkTree(page, object : IPerNode {
			@Throws(Exception::class)
			override fun before(n: NodeBase): Any? {
				val factory = generators[n.javaClass];
				if(factory == null || n.testID == null) {
					return null;
				} else {
					val generator = factory.create(n)
					if(generator.skip) {
						return IPerNode.SKIP
					}
					suitableNodes.add(n);
					generator.run()
					if(!generator.examineChildren()) {
						return IPerNode.SKIP
					}
				}
				return null
			}

			@Throws(Exception::class)
			override fun after(n: NodeBase): Any? {
				return null
			}
		})
		return suitableNodes
	}
	fun generated(list: Collection<NodeBase>, className: String? = null, namespace: String? = null, printerFactory: (GeneratedClassModel) -> PageObjectPrinter = {cm -> JavaPageObjectPrinter(cm)}): String {
		val cname = className ?: page.javaClass.simpleName
		val packageName = namespace ?: page.javaClass.packageName;
		val classModel = GeneratedClassModel(cname, packageName)
		list.forEach{
			val generatorFactory = generators[it.javaClass]
			if(generatorFactory != null) {
				val generator =  generatorFactory.create(it)
				generator.run()
				classModel.members.addAll(generator.classMembers())
				classModel.methods.addAll(generator.classMethods())
			}
		}
		return printerFactory.invoke(classModel).print()
	}
}

abstract class POCodeGenerator {
	var skip: Boolean = false
	abstract fun run()
	abstract fun examineChildren(): Boolean
	abstract fun classMembers(): List<GeneratedClassMember>
	abstract fun classMethods(): List<GeneratedClassMethod>
}

data class GeneratedClassModel(val name: String, val namespace: String, val members: MutableList<GeneratedClassMember> = ArrayList<GeneratedClassMember>(), val methods: MutableList<GeneratedClassMethod> = ArrayList<GeneratedClassMethod>())
data class GeneratedParameter(internal val name: String, internal val type: Class<*>)
data class GeneratedClassMember(internal val name: String, internal val type: Class<*>, internal val accessModifier: POAccessModifier, internal val params: List<String>)
data class GeneratedClassMethod(internal val name: String, internal val type: Class<*>?, internal val accessModifier: POAccessModifier, internal val body: String, internal val parameters: MutableList<GeneratedParameter> = ArrayList())

enum class POAccessModifier {
	PUBLIC, PRIVATE, INTERNAL, PROTECTED
}

abstract class AbstractPOCodeGenerator : POCodeGenerator() {
	protected val members: MutableList<GeneratedClassMember> = ArrayList<GeneratedClassMember>();
	protected val methods: MutableList<GeneratedClassMethod> = ArrayList<GeneratedClassMethod>();

	override fun classMembers(): List<GeneratedClassMember> {
		return members
	}

	override fun classMethods(): List<GeneratedClassMethod> {
		return methods
	}

	protected fun testId(testID: String): String {
		return "\"${testID}\""
	}

	protected fun fixme(): String {
		return "FIXME" //the idea is that this string will clearly state to the programer they need to fix this. well, it will not compile anyway.
	}

	override fun examineChildren(): Boolean {
		return false
	}
}

interface PageObjectPrinter {
	fun print(): String
}
