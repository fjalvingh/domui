package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.component2.lookupinput.LookupInputBase2.IPopupOpener;
import to.etc.domui.dom.html.*;

public class DefaultPopupOpener implements IPopupOpener {
	@Override
	public <A, B, L extends LookupInputBase2<A, B>> Dialog createDialog(final L control, ITableModel<B> initialModel) {
		DefaultLookupInputDialog<A, B> dlg = new DefaultLookupInputDialog<A, B>(control.getQueryMetaModel(), control.getOutputMetaModel(), control.getModelFactory());
		dlg.setOnSelection(new IClicked<DefaultLookupInputDialog<A, B>>() {
			@Override
			public void clicked(DefaultLookupInputDialog<A, B> clickednode) throws Exception {
				B value = clickednode.getValue();
				control.setDialogSelection(value);
			}
		});

		//-- Move all extra stuff needed
		String ttl = control.getDefaultTitle();
		dlg.title(ttl);

		dlg.setQueryHandler(dlg.getQueryHandler());
		dlg.setQueryManipulator(control);


		return dlg;
	}

}
