package to.etc.domui.logic.errors;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Experimental.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 1, 2014
 */
public class ProblemReporter {
	final private NodeBase m_rootNode;

	private Set<UIMessage> m_existingErrorSet;

	private ProblemSet m_errorSet;

	private ProblemModel m_model;

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
			public Object before(NodeBase n) throws Exception {
				if(n instanceof NodeContainer) {
					NodeContainer nc = (NodeContainer) n;
					IErrorFence fence = nc.getErrorFence();
					if(null != fence)
						res.add(fence);
				}

				return null;
			}

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
		Set<IErrorFence> allFences = getAllFences();
		m_existingErrorSet = getAllErrorSet(allFences);
		m_errorSet = m_model.getErrorSet();

		/*
		 * All errors that can be connected to an UI element should go there. By reporting "with"
		 * a component the error shows on the component itself if possible, and will also traverse
		 * to the nearest error panel. Errors that cannot be posted on a component must be shown
		 * globally.
		 *
		 * A solution would be a binding map, but that will cause trouble with gc. So we use the
		 * component tree itself to register errors.
		 */
		DomUtil.walkTree(m_rootNode, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof IBindable) {
					handleClaimError(n);
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});

		//-- All messages that could be claimed are claimed now. Add the rest as "global" messages
		for(ProblemInstance le : m_errorSet) {
			if(!inExistingSet(null, le)) {
				UIMessage ui = UIMessage.create(null, le);
				m_rootNode.addGlobalMessage(ui);
			}
		}

		//-- Now get rid of all that was no longer reported
		for(UIMessage old : m_existingErrorSet) {
			for(IErrorFence f : allFences) {
				f.removeMessage(old);
			}
		}
	}

	/**
	 * Order problems by (severity desc, name asc)
	 */
	static private final Comparator<ProblemInstance> C_BYSEVERITY = new Comparator<ProblemInstance>() {
		@Override
		public int compare(ProblemInstance a, ProblemInstance b) {
			int rc = b.getProblem().getSeverity().getOrder() - a.getProblem().getSeverity().getOrder();
			if(rc != 0)
				return rc;
			return a.getProblem().getMessageKey().compareTo(b.getProblem().getMessageKey());
		}
	};

	/**
	 * For the component, find all bindings on it and check each for errors to be reported. If no error
	 * is found at all the component's error state is cleared.
	 * @param errorSet
	 * @param n
	 */
	private void handleClaimError(@Nonnull NodeBase n) {
		IBindable b = (IBindable) n;
		List<SimpleBinder> bindingList = b.getBindingList();
		if(null == bindingList)
			return;

		//-- Get the errors on all bindings to this component.
		List<ProblemInstance> all = new ArrayList<>();
		List<UIMessage> bindingMessageList = new ArrayList<>();
		for(SimpleBinder binding : bindingList) {
			getErrorsOnBoundProperty(all, n, binding);
			UIMessage be = binding.getBindError();
			if(null != be)
				bindingMessageList.add(be);
		}
		if(all.size() == 0) {
			if(bindingMessageList.size() == 0)
				n.setMessage(null);
			return;
		}

		//-- Sort the errors on severity to get the thing to report @ the component 1st
		Collections.sort(all, C_BYSEVERITY);

		//-- Append these messages to all binding messages, making binding messages the "preferred" one to show @ the control
		IErrorFence fence = DomUtil.getMessageFence(n);
		for(ProblemInstance pi: all) {
			if(! inExistingSet(n, pi)) {
				//-- Needs to be added.
				UIMessage ui = UIMessage.create(n, pi);
				if(n.getMessage() == null) {
					n.setMessage(ui);
				}
				fence.addMessage(ui);
			}
		}
	}

	/**
	 * Check if this message is already in the reported set. If so remove it from the
	 * pending set and return true.
	 *
	 * @param pi
	 * @return
	 */
	private boolean inExistingSet(@Nullable NodeBase node, ProblemInstance pi) {
		for(UIMessage m : m_existingErrorSet) {
			if(m.getMessageKey().equals(pi.getProblem().getMessageKey()) && node == m.getErrorNode()) {
				m_existingErrorSet.remove(m);
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all errors reported on the (instance, property) this binding is bound to.
	 * @param all
	 * @param errorSet
	 * @param n
	 * @param binding
	 */
	private void getErrorsOnBoundProperty(@Nonnull List<ProblemInstance> all, @Nonnull NodeBase n, @Nonnull SimpleBinder binding) {
		Object instance = binding.getInstance();
		if(null == instance)								// Not an instance binding -> no errors here
			return;
		PropertyMetaModel< ? > property = binding.getInstanceProperty();
		if(null == property)								// Not bound to property -> done
			return;
		Collection<ProblemInstance> errors = m_errorSet.remove(instance, property);	// Get and remove errors for this binding
		all.addAll(errors);
	}
}
