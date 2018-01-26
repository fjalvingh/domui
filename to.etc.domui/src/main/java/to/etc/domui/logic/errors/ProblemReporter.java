package to.etc.domui.logic.errors;

import to.etc.domui.component.binding.ComponentPropertyBinding;
import to.etc.domui.component.binding.IBinding;
import to.etc.domui.component.binding.OldBindingHandler;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IValueAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Experimental.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 1, 2014
 */
public class ProblemReporter {
	static private final boolean DEBUG = false;

	final private NodeBase m_rootNode;

	final private ProblemModel m_model;

	public ProblemReporter(NodeBase root, ProblemModel model) {
		m_rootNode = root;
		m_model = model;
	}

	/**
	 * Get all error fences.
	 * @return
	 */
	@Nonnull
	private Set<IErrorFence> getAllFences() throws Exception {
		final Set<IErrorFence> res = new HashSet<>();
		DomUtil.walkTree(m_rootNode, new DomUtil.IPerNode() {
			@Override
			@Nullable
			public Object before(NodeBase n) throws Exception {
				if(n instanceof NodeContainer) {
					NodeContainer nc = (NodeContainer) n;
					IErrorFence fence = nc.getErrorFence();
					if(null != fence)
						res.add(fence);
				}

				return null;
			}

			@Nullable
			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
		return res;
	}

	/**
	 * Get all errors reported on my fences.
	 * @param fenceList
	 * @return
	 */
	@Nonnull
	private Set<UIMessage> getAllErrorSet(@Nonnull Collection<IErrorFence> fenceList) {
		Set<UIMessage> res = new HashSet<>();
		for(IErrorFence f : fenceList)
			res.addAll(f.getMessageList());
		return res;
	}

	public void report() throws Exception {
		//System.out.println("\n\n----- Reporting errors");

		Set<IErrorFence> allFences = getAllFences();
		final Set<UIMessage> existingErrorSet = getAllErrorSet(allFences);
		final ProblemSet newErrorSet = m_model.getErrorSet();

		/*
		 * All errors that can be connected to an UI element should go there. By reporting "with"
		 * a component the error shows on the component itself if possible, and will also traverse
		 * to the nearest error panel. Errors that cannot be posted on a component must be shown
		 * globally.
		 *
		 * A solution would be a binding map, but that will cause trouble with gc. So we use the
		 * component tree itself to register errors.
		 *
		 * Beside: collect the nodes first; handling errors modifies the tree so doing it inside
		 * IPerNode proved a bad idea 8-(
		 */
		final List<NodeBase> bindableNodes = new ArrayList<>();
		DomUtil.walkTree(m_rootNode, new DomUtil.IPerNode() {
			@Override
			@Nullable
			public Object before(NodeBase n) throws Exception {
				List<IBinding> bindingList = n.getBindingList();
				if(null != bindingList && bindingList.size() > 0)
					bindableNodes.add(n);
				return null;
			}

			@Nullable
			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});

		//-- Now handle all
		for(NodeBase n : bindableNodes) {
			//System.out.println(" node: "+desc(n));
			handleClaimError(existingErrorSet, newErrorSet, n);
		}


		//-- All messages that could be claimed are claimed now. Add the rest as "global" messages
		for(ProblemInstance pi : newErrorSet) {
			if(!inExistingSet(existingErrorSet, null, pi)) {
				UIMessage ui = UIMessage.create(null, pi);
				m_rootNode.addGlobalMessage(ui);
			}
		}

		//-- Now get rid of all that was no longer reported
		for(UIMessage old : existingErrorSet) {
			for(IErrorFence f : allFences) {
				if(OldBindingHandler.BINDING_ERROR != old.getGroup())
					f.removeMessage(old);
			}
		}
	}

	/**
	 * Order problems by (severity desc, name asc)
	 */
	static private final Comparator<ProblemInstance> C_BYSEVERITY = new Comparator<ProblemInstance>() {
		@Override
		public int compare(@Nullable ProblemInstance a, @Nullable ProblemInstance b) {
			if(a == null || b == null)
				throw new IllegalStateException();
			int rc = b.getProblem().getSeverity().getOrder() - a.getProblem().getSeverity().getOrder();
			if(rc != 0)
				return rc;
			return a.getProblem().getMessageKey().compareTo(b.getProblem().getMessageKey());
		}
	};

	/**
	 * For the component, find all bindings on it and check each for errors to be reported. If no error
	 * is found at all the component's error state is cleared.
	 * @param existingErrorSet
	 * @param newErrorSet
	 * @param n
	 */
	private void handleClaimError(Set<UIMessage> existingErrorSet, ProblemSet newErrorSet, @Nonnull NodeBase n) {
		//-- Get the errors on all bindings to this component.
		List<ProblemInstance> all = new ArrayList<>();
		List<UIMessage> bindingMessageList = collectBindingErrorsFromComponent(newErrorSet, n, all);
		if(all.size() == 0) {
			if(bindingMessageList.size() == 0) {
				if(DEBUG)
					System.out.println("    er: "+desc(n)+" component error cleared");
				n.setMessage(null);
			}
			if(DEBUG)
				System.out.println("    er: "+desc(n)+" 0 claimed, "+bindingMessageList.size()+" bind errors - not reporting");
			return;
		}

		//-- Sort the errors on severity to get the thing to report @ the component 1st
		all.sort(C_BYSEVERITY);

		//-- Append these messages to all binding messages, making binding messages the "preferred" one to show @ the control
		moveMessageToComponent(existingErrorSet, n, all);
	}

	private void moveMessageToComponent(Set<UIMessage> existingErrorSet, @Nonnull NodeBase n, List<ProblemInstance> all) {
		//IErrorFence fence = DomUtil.getMessageFence(n);
		for(ProblemInstance pi: all) {
			if(!inExistingSet(existingErrorSet, n, pi)) {
				//-- Needs to be added.
				UIMessage ui = UIMessage.create(n, pi);
				//if(n.getMessage() == null) { // jal 20150721
				n.setMessage(ui);
				if(DEBUG)
					System.out.println("    er: "+desc(n)+" component set to "+ui);
				//}
				//fence.addMessage(ui);			// jal 20171024 causes duplicate message because component also registers with fence
				if(DEBUG)
					System.out.println("    er: " + desc(n) + " added " + ui+" to fence");
			} else {
				if(DEBUG)
					System.out.println("    er: "+desc(n)+" existing error "+pi+" already shown");
			}
		}
	}

	@Nonnull private List<UIMessage> collectBindingErrorsFromComponent(ProblemSet newErrorSet, @Nonnull NodeBase n, List<ProblemInstance> all) {
		List<IBinding> bindingList = n.getBindingList();
		if(null == bindingList)
			return Collections.emptyList();

		List<UIMessage> bindingMessageList = new ArrayList<>();
		for(IBinding binding : bindingList) {
			if(binding instanceof ComponentPropertyBinding) {
				ComponentPropertyBinding sib = (ComponentPropertyBinding) binding;
				getErrorsOnBoundProperty(newErrorSet, all, n, sib);
				UIMessage be = binding.getBindError();
				if(null != be)
					bindingMessageList.add(be);
			}
		}
		return bindingMessageList;
	}

	static private String desc(NodeBase n) {
		return "'"+n.getComponentInfo()+"' ("+n.getActualID()+")";
	}

	/**
	 * Check if this message is already in the reported set. If so remove it from the
	 * pending set and return true.
	 *
	 * @param pi
	 * @return
	 */
	private boolean inExistingSet(Set<UIMessage> existingErrorSet, @Nullable NodeBase node, ProblemInstance pi) {
		for(UIMessage m : existingErrorSet) {
			if(node == m.getErrorNode() && m.getMessageKey().equals(pi.getProblem().getMessageKey()) && Arrays.equals(m.getParameters(), pi.getParameters())) {
				existingErrorSet.remove(m);
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all errors reported on the (instance, property) this binding is bound to.
	 * @param all
	 * @param n
	 * @param binding
	 */
	private void getErrorsOnBoundProperty(ProblemSet newErrorSet, @Nonnull List<ProblemInstance> all, @Nonnull NodeBase n, @Nonnull ComponentPropertyBinding binding) {
		Object instance = binding.getInstance();
		if(null == instance)								// Not an instance binding -> no errors here
			return;
		IValueAccessor< ? > property = binding.getInstanceProperty();
		if(null == property)								// Not bound to property -> done
			return;
		Collection<ProblemInstance> errors = newErrorSet.remove(instance, property);	// Get and remove errors for this binding
		all.addAll(errors);
	}
}
