package to.etc.domui.component.masterchild;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.tbl.ColumnDef;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SortableListModel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.databinding.observables.IObservableList;
import to.etc.domui.dom.html.Div;
import to.etc.domui.themes.Theme;
import to.etc.function.ConsumerEx;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QField;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-18.
 */
@NonNullByDefault
public class ChildFragment<P, C> extends Div {
	private final Class<C> m_childClass;

	private final DataTable<C> m_table = new DataTable<>();

	private final P m_parent;

	@Nullable
	private RowRenderer<C> m_renderer;

	@Nullable
	private List<C> m_value;

	@Nullable
	private ConsumerEx<C> m_onNew;

	public ChildFragment(P parent, QField<P, C> property) throws Exception {
		m_parent = parent;
		PropertyMetaModel<C> pmm = MetaManager.getPropertyMeta(parent.getClass(), property);
		if(pmm.getRelationType() != PropertyRelationType.DOWN)
			throw new ProgrammerErrorException("The property " + property + " must be a relation property to a child collection (list of children)");
		if(! List.class.isAssignableFrom(pmm.getActualType()))
			throw new ProgrammerErrorException("The property " + property + " must be of type List<C> (it is a " + pmm.getActualType().getName() + ")");

		Type genericType = pmm.getGenericActualType();
		Class<C> childType = (Class<C>) MetaManager.findCollectionType(genericType);
		if(null == childType)
			throw new ProgrammerErrorException("The jokers that created Java's generics have erased the type of the collection, probably, so I cannot find the collection type of " + property);
		m_childClass = childType;
		bind().to(parent, property);
		m_table.setPageSize(10);
	}

	@Override public void createContent() throws Exception {
		add(m_table);
		m_table.setRowRenderer(getRenderer());
		List<C> value = getValue();
		if(null == value) {
			value = Collections.emptyList();
		}
		if(value instanceof IObservableList) {
			m_table.setList((IObservableList<C>) value);
		} else {
			SortableListModel<C> model = new SortableListModel<>(m_childClass, value);
			m_table.setModel(model);
		}

		ConsumerEx<C> onNew = m_onNew;
		if(onNew != null) {
			ButtonBar2 bb = new ButtonBar2();
			add(bb);
			bb.addButton("Add", Theme.BTN_PLUS, a -> {
				C c = m_childClass.newInstance();
				PropertyMetaModel<P> parentProperty = findParentProperty();
				parentProperty.setValue(c, m_parent);
				onNew.accept(c);
			});
		}
	}

	/**
	 * FIXME This belongs in the metamodel, I'll add it once we know whether this is useful.
	 */
	private PropertyMetaModel<P> findParentProperty() {
		ClassMetaModel cmm = MetaManager.findClassMeta(m_childClass);
		ClassMetaModel parentcmm = MetaManager.findClassMeta(m_parent.getClass());
		for(PropertyMetaModel<?> property : cmm.getProperties()) {
			if(property.getActualType() == parentcmm.getActualClass()) {
				return (PropertyMetaModel<P>) property;
			}
		}
		throw new IllegalStateException("I cannot find the parent property in " + cmm);
	}

	public RowRenderer<C> getRenderer() {
		RowRenderer<C> renderer = m_renderer;
		if(null == renderer) {
			renderer = m_renderer = new RowRenderer<>(m_childClass);
		}
		return renderer;
	}

	public void setRenderer(@Nullable RowRenderer<C> renderer) {
		if(m_renderer == renderer)
			return;
		m_renderer = renderer;
		forceRebuild();
	}

	@Nullable
	public List<C> getValue() {
		return m_value;
	}

	public void setValue(@Nullable List<C> value) {
		if(m_value == value)							// Using == is intentional: using equals() is hideously expensive!
			return;
		m_value = value;
		forceRebuild();
	}

	public void setPageSize(int pageSize) {
		m_table.setPageSize(pageSize);
	}

	public ColumnDef<C, C> column() {
		return getRenderer().column();
	}

	public <V> ColumnDef<C, V> column(QField<C, V> field) {
		return getRenderer().column(field);
	}

	public void onClick(ConsumerEx<C> listener) {
		getRenderer().setRowClicked(listener::accept);
	}

	public void setOnNew(ConsumerEx<C> onNew) {
		m_onNew = onNew;
	}
}
