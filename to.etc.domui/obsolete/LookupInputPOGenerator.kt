package to.etc.domui.uitest.pogenerator

import to.etc.domui.component2.lookupinput.LookupInput2
import to.etc.domui.dom.html.NodeBase
import to.etc.domui.webdriver.core.proxies.LookupPO
import to.etc.util.StringTool

class LookupFactory : POFactory {
	override fun create(s: NodeBase): POCodeGenerator {
		return LookupGenerator(s as LookupInput2<*>)
	}
}

class LookupGenerator(private val lookup: LookupInput2<*>): AbstractPOCodeGenerator() {
	override fun run() {
		members.add(GeneratedClassMember(StringTool.strDecapitalizedIntact(lookup.testID), LookupPO::class.java, POAccessModifier.PRIVATE, listOf("wd()", testId(lookup.testID))))
	}
}
