package to.etc.domui.component.form;

public interface IModelBinding {
	public void moveModelToControl() throws Exception;

	public void moveControlToModel() throws Exception;

	public void setControlsEnabled(boolean on);
}
