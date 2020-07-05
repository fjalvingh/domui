package to.etc.domui.webdriver.core.pogeneration

import to.etc.domui.dom.html.Button
import to.etc.domui.dom.html.NodeBase
import to.etc.domui.webdriver.core.proxies.ButtonPO
import to.etc.util.StringTool

class ButtonFactory : POFactory {
	override fun create(s: NodeBase) : POCodeGenerator {
		return ButtonGenerator(s as Button);
	}
}

class ButtonGenerator(private val btn: Button) : AbstractPOCodeGenerator() {
	override fun run() {
		members.add(GeneratedClassMember(StringTool.strDecapitalizedIntact(btn.testID), ButtonPO::class.java, POAccessModifier.PRIVATE, listOf("wd()", testId(btn.testID))));
	}
}
