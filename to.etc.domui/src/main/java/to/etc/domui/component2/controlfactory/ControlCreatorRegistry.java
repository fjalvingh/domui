package to.etc.domui.component2.controlfactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registers all l2 control creators.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 27, 2014
 */
public class ControlCreatorRegistry {
	@NonNull
	private List<IControlCreator> m_controlFactoryList = new ArrayList<>();

	public ControlCreatorRegistry() {
		//-- Register the default factories
		register(new ControlCreatorDate());
		register(new ControlCreatorEnumAndBool());
		register(new ControlCreatorMoney());
		register(new ControlCreatorRelationCombo());
		register(new ControlCreatorRelationLookup());
		register(new ControlCreatorString());
		register(new ControlCreatorTextArea());
		register(new ControlCreatorBoolean());
		register(new ControlCreatorOldString());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories for editing..						*/
	/*--------------------------------------------------------------*/
	/**
	 * Register a new factory.
	 * @param cf
	 */
	public synchronized void register(@NonNull final IControlCreator cf) {
		ArrayList<IControlCreator> list = new ArrayList<>(m_controlFactoryList);
		list.add(cf);
		m_controlFactoryList = Collections.unmodifiableList(list);
	}

	@NonNull
	protected synchronized List<IControlCreator> getControlFactoryList() {
		return m_controlFactoryList;
	}

	@NonNull
	public <T, C extends IControl<T>> C createControl(@NonNull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		IControlCreator bestcc = null;
		int bestScore = 0;
		for(IControlCreator cc : getControlFactoryList()) {
			int score = cc.accepts(pmm, controlClass);
			if(score > bestScore) {
				bestcc = cc;
				bestScore = score;
			}
		}
		if(bestcc == null)
			throw new IllegalStateException("No control factory found for " + pmm + " and class=" + controlClass);
		return bestcc.createControl(pmm, controlClass);
	}
}
