package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IFbComponent {
	public void drawSelector(@Nonnull NodeContainer container) throws Exception;

	@Nonnull
	public String getShortName();

	@Nonnull
	public String getLongName();

	@Nonnull
	public String getCategoryName();
}
