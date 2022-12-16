package to.etc.domui.component2.lookupinput;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.layout.IWindowClosed;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.tbl.IClickableRowRenderer;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.component2.lookupinput.LookupInputBase2.IPopupOpener;
import to.etc.domui.dom.html.IClicked;
import to.etc.function.IExecute;

import java.util.List;

public class DefaultPopupOpener<A, B> implements IPopupOpener {

	@Nullable
	private List<SearchPropertyMetaModel> m_searchPropertyList;

	@Nullable
	private IClickableRowRenderer<B> m_formRowRenderer;

	@Nullable
	public List<SearchPropertyMetaModel> getSearchPropertyList() {
		return m_searchPropertyList;
	}

	/** The search properties to use in the lookup form when created. If null uses the default attributes on the class. */
	public void setSearchPropertyList(@Nullable List<SearchPropertyMetaModel> searchPropertyList) {
		m_searchPropertyList = searchPropertyList;
	}

	/**
	 * Returns configured custom {@link IClickableRowRenderer}&lt;OT&gt; render for rows when the popup lookup form is used.
	 *
	 * @return configured renderer.
	 */
	@Nullable
	public IClickableRowRenderer<B> getFormRowRenderer() {
		return m_formRowRenderer;
	}

	/**
	 * Sets custom {@link IClickableRowRenderer}&lt;B&gt; render.
	 *
	 * @param lookupFormRenderer render for table rows when the popup lookup form is used.
	 */
	public void setFormRowRenderer(@Nullable IClickableRowRenderer<B> lookupFormRenderer) {
		m_formRowRenderer = lookupFormRenderer;
	}

	@Override
	public <A, B, L extends LookupInputBase2<A, B>> Dialog createDialog(final L control, ITableModel<B> initialModel, final IExecute callOnWindowClose) {
		DefaultLookupInputDialog<A, B> dlg = new DefaultLookupInputDialog<A, B>(control.getQueryMetaModel(), control.getOutputMetaModel(), control.getModelFactory());
		dlg.setOnSelection(new IClicked<DefaultLookupInputDialog<A, B>>() {
			@Override
			public void clicked(DefaultLookupInputDialog<A, B> clickednode) throws Exception {
				B value = clickednode.getValue();
				control.setDialogSelection(value);
			}
		});

		dlg.setSearchProperties(getSearchPropertyList());

		//-- Move all extra stuff needed
		String ttl = control.getDefaultTitle();
		dlg.title(ttl);
		dlg.setTestID(control.getTestID());

		dlg.setQueryHandler(control.getQueryHandler());
		dlg.setQueryManipulator(control);

		if(null != initialModel){
			dlg.setInitialModel(initialModel);
		}

		dlg.setOnClose(new IWindowClosed() {
			@Override public void closed(@NonNull String closeReason) throws Exception {
				callOnWindowClose.execute();
			}
		});

		IClickableRowRenderer<B> formRowRenderer = (IClickableRowRenderer<B>) getFormRowRenderer();
		if(null != formRowRenderer) {
			dlg.setFormRowRenderer(formRowRenderer);
		}

		return dlg;
	}

}
