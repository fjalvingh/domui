package to.etc.domui.webdriver.core.pogeneration

import to.etc.domui.dom.html.NodeBase

class SkipPOFactory : POFactory {
	override fun create(s: NodeBase): POCodeGenerator {
		return SkipPOCodeGenerator()
	}
}
class SkipPOCodeGenerator : AbstractPOCodeGenerator() {
	init {
		skip = true
	}
	override fun run() {

	}
}
