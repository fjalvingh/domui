package to.etc.domui.uitest.pogenerator

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
