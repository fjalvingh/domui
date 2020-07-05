package to.etc.domui.webdriver.core.pogeneration

import to.etc.domui.component.input.Text2
import to.etc.domui.dom.html.NodeBase
import to.etc.domui.webdriver.core.proxies.Text2PO
import to.etc.util.StringTool

class Text2Factory : POFactory {
	override fun create(s: NodeBase): POCodeGenerator {
		return Text2Generator(s as Text2<*>);
	}
}

class Text2Generator(private val txt: Text2<*>) : AbstractPOCodeGenerator() {
	override fun run() {
		members.add(GeneratedClassMember(StringTool.strDecapitalizedIntact(txt.testID), Text2PO::class.java, POAccessModifier.PRIVATE, listOf("wd()", testId(txt.testID))));
	}
}
