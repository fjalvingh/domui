package to.etc.domui.component.binding;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.DomUtil;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
@DefaultNonNull
public class DefaultBindingHandler {
	private final NodeBase m_rootNode;

	static public class BindingPair<CV, PV> {
		private final IBinding m_binding;

		private final CV m_controlValue;

		private final PV m_propertyValue;

		public BindingPair(IBinding binding, CV controlValue, PV propertyValue) {
			m_binding = binding;
			m_controlValue = controlValue;
			m_propertyValue = propertyValue;
		}
	}

	public DefaultBindingHandler(NodeBase rootNode) {
		m_rootNode = rootNode;
	}

	/**
	 * System helper method to move all bindings from control into the model (called at request start).
	 * @throws Exception
	 */
	public void controlToModel() throws Exception {
		List<BindingPair<?, ?>> pairs = collectChangedBindings();
		if(pairs.size() == 0)
			return;



	}

	/**
	 * Find all bindings where the control value and the property value
	 * are different. Return the bindings as a list ordered as follows:
	 * <ul>
	 *     <li>Deeper components <i>before</i> higher components</li>
	 *     <li>Components earlier in DOM order earlier in the list (i.e. top to bottom)</li>
	 * </ul>
	 * @throws Exception
	 */
	private List<BindingPair<?, ?>>  collectChangedBindings() throws Exception {
		List<BindingPair<?, ?>> result = new ArrayList<>();

		DomUtil.walkTree(m_rootNode, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list) {
						BindingPair<?, ?> pair = checkBindingDelta(n, sb);
						if(null != pair)
							result.add(pair);
					}
				}
				return null;
			}
		});
		return result;
	}

	/**
	 * For the binding, check if the binding's property and control value
	 * are different, if so return a Pair describing both values.
	 *
	 * @param n
	 * @param sb
	 * @return
	 */
	@Nullable
	private BindingPair<?, ?> checkBindingDelta(NodeBase n, IBinding sb) {





	}


}
