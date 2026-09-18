package to.etc.domuidemo.pages.tutorial.meta;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.nls.NlsContext;

/**
 * Tutorial, "metadata", step 2: where the text of an enum value comes from. Not
 * one of the labels on this screen is in the Java code - they are in
 * ShippingMethod.properties, ShipmentState.properties and Shipment.properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MetaEnumPage extends UrlPage {
	private final Shipment m_shipment = new Shipment();

	@Override
	public void createContent() throws Exception {
		setPageTitle("Labels for enum values");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Labels for enum values"));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_shipment, Shipment_.method()).control();
		fb.property(m_shipment, Shipment_.returnMethod()).control();
		fb.property(m_shipment, Shipment_.insured()).control();
		fb.property(m_shipment, Shipment_.state()).control();

		cp.add(new HTag(2, "The same values, asked for directly"));
		Div box = new Div("dm-tut-q");
		cp.add(box);

		PropertyMetaModel<ShippingMethod> method = MetaManager.getPropertyMeta(Shipment.class, Shipment_.method());
		PropertyMetaModel<ShippingMethod> back = MetaManager.getPropertyMeta(Shipment.class, Shipment_.returnMethod());
		for(ShippingMethod sm : ShippingMethod.values()) {
			box.add("ShippingMethod." + sm.name()
				+ "\n    on the enum:          " + MetaManager.getEnumLabel(sm)
				+ "\n    on Shipment.method:   " + MetaManager.getEnumLabel(method, sm)
				+ "\n    on Shipment.returnMethod: " + MetaManager.getEnumLabel(back, sm)
				+ "\n");
		}
		box.add("\nlocale of this request: " + NlsContext.getLocale() + "\n");
	}
}
