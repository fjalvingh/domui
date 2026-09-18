package to.etc.domuidemo.pages.tutorial.meta;

import to.etc.annotations.GenerateProperties;
import to.etc.domui.component.meta.MetaProperty;
import to.etc.domui.component.meta.YesNoType;

/**
 * The model the "metadata" tutorial pages edit. Everything the screen shows
 * about these properties - their labels, and the labels of the enum values they
 * can hold - comes from Shipment.properties and from the bundles of the enums
 * themselves.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@GenerateProperties
public class Shipment {
	private ShippingMethod m_method = ShippingMethod.Standard;

	private ShippingMethod m_returnMethod;

	private Boolean m_insured;

	private ShipmentState m_state = ShipmentState.Ordered;

	@MetaProperty(required = YesNoType.YES)
	public ShippingMethod getMethod() {
		return m_method;
	}

	public void setMethod(ShippingMethod method) {
		m_method = method;
	}

	public ShippingMethod getReturnMethod() {
		return m_returnMethod;
	}

	public void setReturnMethod(ShippingMethod returnMethod) {
		m_returnMethod = returnMethod;
	}

	public Boolean getInsured() {
		return m_insured;
	}

	public void setInsured(Boolean insured) {
		m_insured = insured;
	}

	public ShipmentState getState() {
		return m_state;
	}

	public void setState(ShipmentState state) {
		m_state = state;
	}
}
