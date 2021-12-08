package to.etc.domui.uitest.pogenerator

import to.etc.domui.component.buttons.DefaultButton
import to.etc.domui.component.layout.Window
import to.etc.domui.component.misc.MsgBox2
import to.etc.domui.component.tbl.DataTable
import to.etc.domui.component.tbl.InstanceSelectionModel
import to.etc.domui.component.tbl.RowRenderer
import to.etc.domui.component2.buttons.ButtonBar2
import to.etc.domui.component2.form4.FormBuilder
import to.etc.domui.databinding.observables.ObservableList
import to.etc.domui.dom.html.Div
import to.etc.domui.dom.html.NodeBase
import to.etc.domui.dom.html.Pre
import to.etc.domui.server.ITestUiCodeGeneratorListener
import to.etc.domui.util.DomUtil

class SuitableNodeSelectionFragment(private val gen: POGenerator) : Div() {

	companion object {
		@JvmStatic
		val defaultTestUiGenerator = object : ITestUiCodeGeneratorListener {
			override fun isShownFor(node: NodeBase): Boolean {
				val body = node.page.body
				val children = body.getDeepChildren(Window::class.java)
				return children.any { "pageObjectGeneration" == it.testID }
			}

			override fun showFor(node: NodeBase) {
				val body = node.page.body
				val generator = POGenerator(body)
				val frag = SuitableNodeSelectionFragment(generator)
				frag.isStretchHeight = true
				val dlg = MsgBox2.on(body)
				dlg.content(frag).title("Page object generation")
				dlg.resizable()
				dlg.size(900, 500)
				dlg.testID = "pageObjectGeneration"
			}
		}
	}

	val ism = InstanceSelectionModel<NodeBase>(true)
	var packageName: String = gen.page.javaClass.packageName
	var className: String = gen.page.javaClass.simpleName
	var code: String? = null
	var codeFragment = Div("ui-po-selection-code")

	override fun createContent() {
		testID = "gen-suitablenode"
		addCssClass("ui-po-selection-node")
		val fb = FormBuilder(this);
		fb.property(this, "className").control()
		fb.property(this, "packageName").control()
		val dt = DataTable<NodeBase>();
		dt.selectionModel = ism
		val rr: RowRenderer<NodeBase> = RowRenderer<NodeBase>(NodeBase::class.java)
		rr.column("testID")
		dt.rowRenderer = rr;

		dt.list = ObservableList(gen.suitableNodes);

		val bb = ButtonBar2()
		add(bb)
		add(dt);
		add(codeFragment)
		bb.addButton("Find suitable nodes") {
			val nodes = gen.findAllSuitable()
			dt.list = ObservableList(nodes)
			ism.setSelectedSet(nodes)
		}
		bb.addButton("Generated out of selected") {
			codeFragment.removeAllChildren()
			code = gen.generated(ism.selectedSet, className, packageName)
			val pre = Pre();
			pre.setText(code)
			codeFragment.add(pre)
			appendJavascript("$('.ui-po-selection-code').scrollTop($('.ui-po-selection-node').height());");
			val btn = codeFragment.add(DefaultButton("Copy to clipboard"))
			DomUtil.clipboardCopy(btn, code)
		}
	}

	fun isCopyDisabled(): Boolean {
		return code == null
	}
}

