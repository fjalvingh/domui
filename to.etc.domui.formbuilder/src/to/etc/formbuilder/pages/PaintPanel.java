package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public class PaintPanel extends Div {
	@Override
	public void createContent() throws Exception {
		setCssClass("fd-pp");


	}

	public void webActionDropComponent(@Nonnull RequestContextImpl ctx) throws Exception {
		System.out.println("Drop event: ");

	}

}
