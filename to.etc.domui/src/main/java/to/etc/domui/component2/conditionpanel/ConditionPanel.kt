package to.etc.domui.component2.conditionpanel

import to.etc.domui.component.input.Text2
import to.etc.domui.component2.combo.ComboLookup2
import to.etc.domui.databinding.observables.ObservableList
import to.etc.domui.dom.html.Div
import to.etc.domui.dom.html.IControl
import to.etc.domui.dom.html.NodeBase
import to.etc.webapp.query.QOperation
import java.util.function.Supplier

interface IConditionModel<T, F> {
	fun getFields(): List<F>

	fun fieldName(field: F): String

	fun allowedOperations(field: F): Set<QOperation>
}

class ConditionPanel<T, F>(val model: IConditionModel<T, F>) : Div("ui-copa") {
	val rootNode = CoCompound<T, F>(null)
	val rootContainer = Div("ui-copa-root")
	val rootCompound = CondUiCompound<T, F>(this, rootNode)

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
		rootContainer.add(rootCompound)
	}

	/**
	 * Ensure there is always at least one simple node present.
	 */
	fun fixModel() {
		if(rootNode.conditions.size == 0) {
			rootNode.conditions.add(CoSimple<T, F>(rootNode))
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
		triple.add(fieldC as NodeBase)
		val operatorC = ComboLookup2<QOperation>()
		operatorC.isMandatory = true
		triple.add(operatorC)
		val valueContainer = triple.add(Div("ui-copa-cmp-val"))

		//-- Listeners
		fieldC.setOnValueChanged {
			updateControls(valueContainer, operatorC, fieldC.value)
		}
		operatorC.setOnValueChanged {
			updateControls(valueContainer, operatorC, fieldC.value)
		}
		updateControls(valueContainer, operatorC, node.field)
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
open class CondUiCompound<T, F>(panel: ConditionPanel<T, F>, val node: CoCompound<T, F>) : CondUiBase<T, F>(panel, "ui-copa-list") {
	private val container = Div("ui-copa-co-cont")

	override fun createContent() {
		add(container)
		for(condition in node.conditions) {
			when(condition) {
				is CoSimple -> renderSimple(condition)
				is CoCompound -> renderCompound(condition)
			}
		}
	}

	private fun renderCompound(condition: CoCompound<T, F>) {
		container.add(CondUiCompound(panel, condition))
	}

	private fun renderSimple(condition: CoSimple<T, F>) {
		container.add(CondUiSimple(panel, condition))
	}
}

sealed class CoNode<T, F>(val parent: CoCompound<T, F>?) {}

class CoCompound<T, F>(parent: CoCompound<T, F>?) : CoNode<T, F>(parent) {
	val conditions = ObservableList<CoNode<T, F>>()
}

class CoSimple<T, F>(parent: CoCompound<T, F>) : CoNode<T, F>(parent) {
	var operation: QOperation? = null
	var field: F? = null
	var value: Any? = null

	fun isEmpty() : Boolean {
		return operation == null || field == null || (value == null && !operation!!.isParameterLess())
	}
}

fun QOperation.isParameterLess() : Boolean {
	return this == QOperation.ISNOTNULL || this == QOperation.ISNULL
}

