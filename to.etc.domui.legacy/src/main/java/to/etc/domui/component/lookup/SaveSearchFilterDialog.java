package to.etc.domui.component.lookup;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.OldBindingHandler;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.input.TextStr;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.lookup.filter.LookupFilterTranslator;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.CodeException;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;

import java.util.Map;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/25/16.
 */
@NonNullByDefault
public final class SaveSearchFilterDialog extends Dialog {

	private final ILookupFilterHandler m_lookupFilterHandler;

	private final String m_pageName;

	private Map<String, ?> m_filterValues;

	@Nullable
	private INotify<SavedFilter> m_onFilterSaved;

	public SaveSearchFilterDialog(ILookupFilterHandler lookupFilterHandler, String pageName, Map<String, ?> filterValues) {
		super(true, false, 500, 200, "Filter opslaan");
		m_lookupFilterHandler = lookupFilterHandler;
		m_pageName = pageName;
		m_filterValues = filterValues;
	}

	public void onFilterSaved(@Nullable INotify<SavedFilter> onFilterSaved) {
		m_onFilterSaved = onFilterSaved;
	}

	@Override
	public void createContent() throws Exception {
		createButtons();

		FormBuilder fb = new FormBuilder(this);
		final TextStr searchName = new TextStr();
		fb.label("Filter").mandatory().item(searchName);
		searchName.setSize(25);
		searchName.setMaxLength(128);

		setOnSave(new IExecute() {
			@Override
			public void execute() throws Exception {
				String filterName = DomUtil.nullChecked(searchName.getValue());
				String filterQuery = LookupFilterTranslator.serialize(m_filterValues);

				Long savedFilterId;
				try(QDataContext unmanagedContext = QContextManager.createUnmanagedContext()) { // We create a separate context because we don't want to commit other transactions
					savedFilterId = m_lookupFilterHandler.save(unmanagedContext, m_pageName, filterName, filterQuery);
					unmanagedContext.commit();
				} catch (CodeException x) {
					MessageFlare.display(getPage().getBody(), MsgType.ERROR, x.getMessage());
					return;
				}
				SavedFilter savedFilter = new SavedFilter(savedFilterId, filterName, filterQuery);
				if(m_onFilterSaved != null) {
					m_onFilterSaved.onNotify(savedFilter);
				}

				MessageFlare.display(getPage().getBody(), MsgType.INFO, Msgs.BUNDLE.getString(Msgs.SAVE_SEARCH_DIALOG_NAME_SAVED));
			}
		});
	}

	@Override
	protected boolean onValidate() throws Exception {
		clearGlobalMessage();

		// Check required field
		boolean hasBindingErrors = OldBindingHandler.reportBindingErrors(this);
		return !hasBindingErrors;
	}
}
