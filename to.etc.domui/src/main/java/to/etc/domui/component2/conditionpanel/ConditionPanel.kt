package to.etc.domui.component2.conditionpanel

import to.etc.domui.databinding.observables.ObservableList
import to.etc.domui.dom.html.Div
import to.etc.webapp.query.QOperation

interface IConditionModel<T, F> {
	fun getFields(): List<F>

	fun fieldName(field: F): String

	fun allowedOperations(field: F): Set<QOperation>
}

class ConditionPanel<T, F>(val model: IConditionModel<T, F>) : Div("ui-copa") {

}

open class CondUiBase<T, F>(val panel: ConditionPanel<T, F>, css: String) : Div(css) {

}

/**
 * UI for a simple condition in the form FIELD OPERATION VALUE.
 */
open class CondUiSimple<T, F>(panel: ConditionPanel<T, F>) : CondUiBase<T, F>(panel, "ui-copa-cmp") {

}

/**
 * UI for a list of conditions, contained of both compounds and comparisons.
 */
open class CondUiCompound<T, F>(panel: ConditionPanel<T, F>) : CondUiBase<T, F>(panel, "ui-copa-list") {

}

sealed internal class CoNode<T, F>(val parent: CoNode<T, F>?) {}

internal class CoCompound<T, F>(val parent: CoCompound<T, F>?) {
	val conditions = ObservableList<CoNode<T, F>>()
}

internal class CoSimple<T, F>(var parent: CoCompound<T, F>) {
	var operation: QOperation? = null
	var field: F? = null
	var value: Any? = null
}
