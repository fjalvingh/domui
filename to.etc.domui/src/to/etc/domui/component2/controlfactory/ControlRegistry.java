package to.etc.domui.component2.controlfactory;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

/**
 * Registers all l2 control creators.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 27, 2014
 */
public class ControlRegistry {
	@Nonnull
	private List<IControlCreator> m_controlFactoryList = new ArrayList<>();

	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories for editing..						*/
	/*--------------------------------------------------------------*/
	/**
	 * Register a new factory.
	 * @param cf
	 */
	public synchronized void registerControlFactory(@Nonnull final IControlCreator cf) {
		ArrayList<IControlCreator> list = new ArrayList<>(m_controlFactoryList);
		list.add(cf);
		m_controlFactoryList = Collections.unmodifiableList(list);
	}

	@Nonnull
	protected synchronized List<IControlCreator> getControlFactoryList() {
		return m_controlFactoryList;
	}

	@Nonnull
	public <T, C extends IControl<T>> C createControl(@Nonnull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		IControlCreator bestcc = null;
		int bestScore = -1;
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
