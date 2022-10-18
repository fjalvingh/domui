package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.DomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
@NonNullByDefault
public class DefaultBindingHandler implements IBindingHandler {
	static public final IBindingHandlerFactory FACTORY = new IBindingHandlerFactory() {
		@NonNull
		@Override
		public IBindingHandler getBindingHandler(@NonNull NodeBase node) {
			return new DefaultBindingHandler(node);
		}
	};

	private final NodeBase m_rootNode;

	public DefaultBindingHandler(NodeBase rootNode) {
		m_rootNode = rootNode;
	}

	/**
	 * System helper method to move all bindings from control into the model (called at request start). This
	 * detects all bindings that changed, and then moves them in the correct order.
	 */
	@Override
	public void controlToModel() throws Exception {
		List<BindingValuePair<?>> pairs = collectChangedBindings();
		if(pairs.isEmpty())
			return;

		//-- We now know all bindings that changed values, and the list is in the proper order. Move all data.
		for(BindingValuePair<?> pair : pairs) {
			pair.moveControlToModel();
		}
	}

	/**
	 * Find all bindings where the control value and the property value
	 * are different. Return the bindings as a list ordered as follows:
	 * <ul>
	 *     <li>Deeper components <i>before</i> higher components</li>
	 *     <li>Components earlier in DOM order earlier in the list (i.e. top to bottom)</li>
	 * </ul>
	 */
	private List<BindingValuePair<?>> collectChangedBindings() throws Exception {
		List<BindingValuePair<?>> result = new ArrayList<>();

		DomUtil.walkTreeUndelegated(m_rootNode, new DomUtil.IPerNode() {
			@Nullable
			@Override
			public Object before(NodeBase n) throws Exception {
				return null;
			}

			@Override
			@Nullable
			public Object after(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list) {
						BindingValuePair<?> pair = sb.getBindingDifference();
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
	 * Move all bindings from model to control (called at request end). We move data from parent nodes
	 * before the data for its children is moved. This should allow components to use binding internally
	 * too.
	 */
	@Override
	public void modelToControl() throws Exception {
		DomUtil.walkTreeUndelegated(m_rootNode, new DomUtil.IPerNode() {
			@Override
			@Nullable
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list)
						sb.moveModelToControl();
				}
				if(n instanceof IModelToControlListener) {
					((IModelToControlListener) n).onModelToControl();
				}
				return null;
			}

			@Override
			@Nullable
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
	}
}
