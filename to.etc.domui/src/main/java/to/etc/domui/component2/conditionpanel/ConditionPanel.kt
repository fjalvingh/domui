package to.etc.domui.component2.conditionpanel

import to.etc.domui.component.buttons.LinkButton
import to.etc.domui.component.input.Text2
import to.etc.domui.component.misc.Icon
import to.etc.domui.component.misc.MessageFlare
import to.etc.domui.component.misc.MsgBox
import to.etc.domui.component2.combo.ComboLookup2
import to.etc.domui.databinding.list.ListChangeAdd
import to.etc.domui.databinding.list.ListChangeAssign
import to.etc.domui.databinding.list.ListChangeDelete
import to.etc.domui.databinding.list.ListChangeModify
import to.etc.domui.databinding.list2.IListChangeListener
import to.etc.domui.databinding.list2.IListChangeVisitor
import to.etc.domui.databinding.observables.ObservableList
import to.etc.domui.dom.errors.MsgType
import to.etc.domui.dom.html.Div
import to.etc.domui.dom.html.IControl
import to.etc.domui.dom.html.NodeBase
import to.etc.domui.dom.html.Span
import to.etc.webapp.query.QOperation
import java.util.function.Supplier

interface IConditionModel<T, F> {
	fun getFields(): List<F>

	fun fieldName(field: F): String

	fun allowedOperations(field: F): Set<QOperation>
}

class ConditionPanel<T, F>(val model: IConditionModel<T, F>) : Div("ui-copa") {
	var rootNode = CoCompound<T, F>(QOperation.AND)
	val rootContainer = Div("ui-copa-root")
//	val rootCompound = CondUiCompound<T, F>(this, rootNode)

	/**
	 * Factory to create a component to select a field; defaults to a ComboLookup2.
	 */
	var fieldSelectorFactory: Supplier<IControl<F>> = object: Supplier<IControl<F>> {
		override fun get(): IControl<F> {
			return ComboLookup2<F>(model.getFields(), "name")
		}
	}

	var fieldValueControlFactory: java.util.function.Function<F, IControl<*>> = object: java.util.function.Function<F, IControl<*>> {
		override fun apply(t: F): IControl<*> {
			return Text2<String>(String::class.java)
		}
	}

	override fun createContent() {
		fixModel()
		add(rootContainer)
		rootContainer.add(CondUiCompound(this, rootNode))
	}

	/**
	 * Ensure there is always at least one simple node present.
	 */
	fun fixModel() {
		if(rootNode.conditions.size == 0) {
			rootNode.conditions.add(CoSimple<T, F>())
		}
	}
}

open class CondUiBase<T, F>(val panel: ConditionPanel<T, F>, css: String) : Div(css) {

}

/**
 * UI for a simple condition in the form FIELD OPERATION VALUE.
 */
open class CondUiSimple<T, F>(panel: ConditionPanel<T, F>, val node: CoSimple<T, F>) : CondUiBase<T, F>(panel, "ui-copa-cmp") {
	private var currentOperation: QOperation? = null

	private var currentField: F? = null

	override fun createContent() {
		val triple = add(Div("ui-copa-cmp-c"))
		val fieldC = panel.fieldSelectorFactory.get()
		fieldC.isMandatory = true
		triple.add(fieldC as NodeBase)
		val operatorC = ComboLookup2<QOperation>()
		operatorC.isMandatory = true
		triple.add(operatorC)
		val valueContainer = triple.add(Div("ui-copa-cmp-val"))
		currentField = node.field
		currentOperation = node.operation

		//-- Listeners
		fieldC.setOnValueChanged {
			updateControls(valueContainer, operatorC, safeValue(fieldC))
		}
		operatorC.setOnValueChanged {
			updateControls(valueContainer, operatorC, safeValue(fieldC))
		}
		updateControls(valueContainer, operatorC, node.field)

		//-- Bindings
		fieldC.bind().to(node, "field")
		operatorC.bind().to(node, "operation")

		//-- Action
		val acd = Div("ui-copa-cmp-ac")
		triple.add(acd)
		acd.add(LinkButton("Delete", Icon.faMinus) {
			if(node.isEmpty()) {
				deleteSimple()
			} else {
				MsgBox.yesNo(this, "Delete?", {
					it: MsgBox ->
					deleteSimple()
				})
			}
		})

		val operator = if(node.parent!!.operation == QOperation.AND) QOperation.OR else QOperation.AND

		acd.add(LinkButton(operator.name, Icon.faList) {
			addCompound(operator)
		})
	}

	private fun deleteSimple() {
		val parent = node.parent!!
		parent.remove(node)
		parent.simplify()
	}

	private fun <T> safeValue(control: IControl<T>) : T? {
		try {
			return control.valueSafe
		} catch(x: Exception) {
			control.clearMessage()
			return null
		}
	}

	private fun addCompound(operator: QOperation) {
		val p = node.parent!!

		//-- If the parent already has a node of the required join type -> move this node there.
		for(condition in p.conditions) {
			if(condition is CoCompound) {
				if(condition.operation == operator) {
					p.remove(node)
					condition.add(node)
					return
				}
			}
		}

		//-- We need a new one..
		val nw = CoCompound<T,F>(operator)
		val index = p.conditions.indexOf(node)
		p.remove(node)
		nw.add(node)
		nw.add(CoSimple<T, F>())
		p.add(index, nw)
	}

	private fun updateControls(valueContainer: Div, operationC: ComboLookup2<QOperation>, field: F?) {
		if(field != null) {
			val operations = panel.model.allowedOperations(field)
			var op = try { operationC.bindValue } catch(x: Exception) { null }
			operationC.data = ArrayList(operations)
			if(op == null || op !in operations) {
				if(QOperation.EQ in operations) {
					op = QOperation.EQ
				} else {
					op = operations.first()
				}
			}
			operationC.value = op

			if(field != currentField) {
				currentField = field
				node.value = null
			}

			valueContainer.removeAllChildren()
			if(op.isParameterLess()) {
				node.value = null
			} else {
				val valueControl = panel.fieldValueControlFactory.apply(field)
				valueContainer.add(valueControl as NodeBase)
				valueControl.bind().to(this, "node.value")
			}
		} else {
			operationC.data = ArrayList()
			operationC.value = null
			valueContainer.removeAllChildren()
			node.value = null
		}
	}
}

/**
 * UI for a list of conditions, contained of both compounds and comparisons.
 */
open class CondUiCompound<T, F>(panel: ConditionPanel<T, F>, val node: CoCompound<T, F>) : CondUiBase<T, F>(panel, "ui-copa-grp") {
	private val container = Div("ui-copa-grp-list")

	override fun createContent() {
		addCssClass("ui-copa-grp-l${node.level()}")

		val andor = Div("ui-copa-grp-type ui-copa-grp-type-${node.operation.name.toLowerCase()}")
		add(andor)
		andor.add(Span(node.operation.name))
		add(container)
		for(condition in node.conditions) {
			container.add(createUi(condition))
		}

		//-- We finish with the "add" action
		val acd = Div("ui-copa-grp-ac")
		container.add(acd)
		acd.add(LinkButton("Add a condition", Icon.faPlus) {
			addCondition()
		})

		node.conditions.addChangeListener(IListChangeListener { event ->
			for(change in event.getChanges()) {
				change.visit(object: IListChangeVisitor<CoNode<T, F>> {
					override fun visitAssign(assign: ListChangeAssign<CoNode<T, F>>) {
						TODO("Not yet implemented")
					}

					override fun visitAdd(add: ListChangeAdd<CoNode<T, F>>) {
						container.add(add.index, createUi(add.value))
					}

					override fun visitDelete(add: ListChangeDelete<CoNode<T, F>>) {
						container.removeChild(add.index)
					}

					override fun visitModify(add: ListChangeModify<CoNode<T, F>>) {
						TODO("Not yet implemented")
					}
				})
			}
		})

	}

	private fun addCondition() {
		for(condition in node.conditions) {
			if(condition is CoSimple) {
				if(condition.isEmpty()) {
					MessageFlare.display(this, MsgType.ERROR, "There is already an empty condition here, fill it in 1st")
					return
				}
			}
		}

		node.add(CoSimple<T, F>())
	}

	private fun createUi(condition: CoNode<T, F>) : CondUiBase<T, F> {
		return when(condition) {
			is CoSimple -> CondUiSimple(panel, condition)
			is CoCompound -> CondUiCompound(panel, condition)
		}
	}
}

sealed class CoNode<T, F>() {
	var parent: CoCompound<T, F>? = null
}

class CoCompound<T, F>(val operation: QOperation) : CoNode<T, F>() {
	val conditions = ObservableList<CoNode<T, F>>()

	fun add(node: CoNode<T, F>) {
		conditions.add(node)
		node.parent = this
	}
	fun add(index: Int, node: CoNode<T, F>) {
		conditions.add(index, node)
		node.parent = this
	}

	fun remove(node: CoNode<T, F>) {
		conditions.remove(node)
		node.parent = null

		//-- If this is the root node: never allow it to be empty
		if(parent == null && conditions.size == 0) {
			add(CoSimple())
		}
	}

	fun level() : Int {
		var c = 0
		var cur = parent
		while(cur != null) {
			cur = cur.parent
			c++
		}
		return c
	}

	/**
	 *
	 */
	fun simplify() {
		val dad = parent ?: return
		if(conditions.size == 1) {
			var index = dad.conditions.indexOf(this)
			dad.remove(this)
			val sub = conditions[0]
			if(sub is CoSimple) {
				dad.add(index, sub)
			} else if(sub is CoCompound) {
				//-- The sub is a compound. Merge all terms in the parent's parent
				dad.remove(sub)
				for(condition in sub.conditions) {
					dad.add(index++, condition)
				}
			} else {
				error("Unknown nodetype")
			}
		} else if(conditions.size == 0) {
			dad.remove(this)
		}
	}
}

class CoSimple<T, F>(var operation: QOperation?, var field: F?, var value: Any?) : CoNode<T, F>() {
	constructor() : this(null, null, null)

	fun isEmpty() : Boolean {
		return operation == null || field == null || (value == null && !operation!!.isParameterLess())
	}
}

fun QOperation.isParameterLess() : Boolean {
	return this == QOperation.ISNOTNULL || this == QOperation.ISNULL
}

