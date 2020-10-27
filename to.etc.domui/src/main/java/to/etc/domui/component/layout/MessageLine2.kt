package to.etc.domui.component.layout

import to.etc.domui.component.misc.IIconRef
import to.etc.domui.dom.errors.MsgType
import to.etc.domui.dom.html.Div
import to.etc.domui.themes.Theme

/**
 * Shows message line with specified or appropriate default icon.
 * Compared to older MessageLine brings a bit larger icons and nice rounded border around message.
 */
class MessageLine2(val type: MsgType, val msg: String, val icon: IIconRef? = null): Div() {

	override fun createContent() {
		super.createContent()
		addCssClass("ui-msgln2")
		when(type) {
			MsgType.INFO -> addCssClass("ui-msgln2-info")
			MsgType.WARNING -> addCssClass("ui-msgln2-warn")
			MsgType.ERROR -> addCssClass("ui-msgln2-err")
		}
		add(msg)
		var aImage = if(null != icon) {
			icon.createNode()
		}else {
			when(type) {
				MsgType.INFO -> Theme.ICON_BIG_INFO.createNode()
				MsgType.WARNING -> Theme.ICON_BIG_WARNING.createNode()
				MsgType.ERROR -> Theme.ICON_BIG_ERROR.createNode()
			}
		}
		aImage.addCssClass("ui-msgln2-icon")
		add(aImage)
	}
}
