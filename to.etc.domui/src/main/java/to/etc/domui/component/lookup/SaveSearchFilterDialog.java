package to.etc.domui.component.lookup;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.binding.*;
import to.etc.domui.component.event.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.filter.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component2.form4.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 1/25/16.
 */
@DefaultNonNull
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
