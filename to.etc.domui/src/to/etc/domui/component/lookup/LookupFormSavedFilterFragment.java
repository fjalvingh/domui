package to.etc.domui.component.lookup;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.event.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

/**
 * Fragment containing the saved filter searches
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/19/16.
 */
@DefaultNonNull
public final class LookupFormSavedFilterFragment extends Div {

	private final List<SavedFilter> m_savedFilters;

	@Nullable
	private INotify<SavedFilter> m_onFilterClicked;

	@Nullable
	private INotify<SavedFilter> m_onFilterDeleted;

	public LookupFormSavedFilterFragment(List<SavedFilter> savedFilters) {
		m_savedFilters = savedFilters;
	}

	public void onFilterClicked(@Nullable INotify<SavedFilter> onFilterClicked) {
		m_onFilterClicked = onFilterClicked;
	}

	public void onFilterDeleted(@Nullable INotify<SavedFilter> onFilterDeleted) {
		m_onFilterDeleted = onFilterDeleted;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		List<SavedFilter> savedFilters = m_savedFilters;
		if(savedFilters.isEmpty()) {
			return;
		}
		Collections.sort(savedFilters, new Comparator<SavedFilter>() {
				@Override
				public int compare(@Nullable SavedFilter o1, @Nullable SavedFilter o2) {
					if(o1 == null) {
						if(o2 == null) {
							return 0;
						}
						return -1;
					}
					else if(o2 == null) {
						return 1;
					}
					return o1.getFilterName().compareToIgnoreCase(o2.getFilterName());
				}
			});
		final Table table = new Table();
		add(table);
		table.setCssClass("ui-lfsf-st");
		table.setTableHead(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_SAVED_FILTERS));
		final TBody body = new TBody();
		body.setTestID("tableBodyLookupSavedFilter");
		table.add(body);
		body.addCssClass("");
		for(final SavedFilter filter: m_savedFilters) {
			ATag filterName = new ATag();
			filterName.setText(filter.getFilterName());
			final TR row = body.addRow();
			row.addCell().add(filterName);
			filterName.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(@Nonnull NodeBase clickednode) throws Exception {
					if(m_onFilterClicked != null) {
						m_onFilterClicked.onNotify(filter);
					}
				}
			});
			SmallImgButton delImage = new SmallImgButton(Theme.BTN_DELETE);
			delImage.setTitle(Msgs.BUNDLE.getString(Msgs.LOOKUP_FORM_DELETE_FILTER));
			body.addCell().add(delImage);
			delImage.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(@Nonnull NodeBase clickednode) throws Exception {
					m_savedFilters.remove(filter);
					if(m_savedFilters.isEmpty()) {
						table.remove();
					} else {
						row.remove();
					}
					if(m_onFilterDeleted != null) {
						m_onFilterDeleted.onNotify(filter);
					}
				}
			});
		}
	}
}
