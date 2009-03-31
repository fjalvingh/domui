package to.etc.domui.component.form;

public interface ModelBinding {
	public void		moveModelToControl() throws Exception;
	public void		moveControlToModel() throws Exception;
	public void		setEnabled(boolean on);
}
