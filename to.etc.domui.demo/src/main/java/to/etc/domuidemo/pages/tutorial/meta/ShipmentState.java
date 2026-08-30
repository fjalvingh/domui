package to.etc.domuidemo.pages.tutorial.meta;

/**
 * The state of a {@link Shipment}. It has more than five values, which is what
 * makes the control factory pick a combobox for it where {@link ShippingMethod}
 * gets a radio group.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum ShipmentState {
	Ordered, Packed, Handed, InTransit, Delivered, Returned
}
