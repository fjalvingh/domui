package to.etc.domui.dom.html;

import to.etc.domui.dom.errors.*;


public interface IInputBase {
	public void clearMessage();

	public UIMessage getMessage();

	public IValueChanged< ? , ? > getOnValueChanged();

	public void setOnValueChanged(IValueChanged< ? , ? > onValueChanged);
}
