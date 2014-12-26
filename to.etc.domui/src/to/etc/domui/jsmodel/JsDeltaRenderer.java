package to.etc.domui.jsmodel;

import to.etc.domui.component.meta.*;
import to.etc.domui.jsmodel.ClassInfo.*;

import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class JsDeltaRenderer {
	private final Appendable m_output;

	private final JsModel m_model;

	private final String m_modelRoot;

	private final Set<InstanceInfo> m_refRenderedSet = new HashSet<>();

	public JsDeltaRenderer(Appendable output, JsModel jsModel, String modelRoot) {
		m_output = output;
		m_model = jsModel;
		m_modelRoot = modelRoot;
	}

	public void render() throws Exception {
		//-- 1. Collect current set of instances.
		Set<InstanceInfo> currentSet = m_model.collectAllInstances(m_model.getRootObject());// Find everything reachable now
		Set<InstanceInfo> oldSet = m_model.getReachableSet();

		//-- Split into items added, removed and retained.
		Set<InstanceInfo> addedSet = new HashSet<>(currentSet);			// Added = current - old
		addedSet.removeAll(oldSet);

		Set<InstanceInfo> removedSet = new HashSet<>(oldSet);
		removedSet.removeAll(currentSet);								// Removed = old - current

		Set<InstanceInfo> retainedSet = new HashSet<>(currentSet);
		retainedSet.retainAll(oldSet);

		m_output.append("function() {\n");

		//-- Now: first render changed properties for all retained things.
		for(InstanceInfo ii: retainedSet) {
			renderPropertyDeltas(ii);
		}
		m_output.append("\n}();\n");
	}

	private String renderRef(InstanceInfo ii) throws Exception {
		if(m_refRenderedSet.add(ii)) {
			m_output.append("v").append(ii.getId()).append("=").append(m_modelRoot).append(".byId('").append(ii.getId()).append("');\n");
		}
		return "v"+ii.getId();
	}

	private void renderPropertyDeltas(InstanceInfo ii) throws Exception {
		ClassInfo ci = ii.getClassInfo();
		boolean done = false;
		for(Simple<?> simple: ci.getSimpleProperties().values()) {
			done = renderPropertyDelta(simple, ii, done);
		}
		for(PropertyMetaModel<?> pm: ci.getParentProperties()) {
			done = renderSimpleParent(pm, ii, done);
		}

		if(done) {
			m_output.append("});\n");
		}
	}

	private <T> boolean renderSimpleParent(PropertyMetaModel<T> pm, InstanceInfo ii, boolean done) {





		return false;
	}

	private <T> boolean renderPropertyDelta(Simple<T> simple, InstanceInfo ii, boolean inited) throws Exception {
		T value = simple.getProperty().getValue(ii.getInstance());
		if(ii.updateValue(simple.getProperty(), value)) {			// Update- if not changed however exit
			return inited;
		}

		if(! inited) {
			String var = renderRef(ii);
			m_output.append(var).append(".updateProperties({");
		} else {
			m_output.append(",");
		}
		m_output.append(simple.getProperty().getName()).append(":");
		simple.getRenderer().render(m_output, value);
		return true;
	}
}
