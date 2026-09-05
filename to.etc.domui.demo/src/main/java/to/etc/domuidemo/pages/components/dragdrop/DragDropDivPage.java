package to.etc.domuidemo.pages.components.dragdrop;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.DropEvent;
import to.etc.domui.util.IDragArea;
import to.etc.domui.util.IDragHandler;
import to.etc.domui.util.IDropHandler;

/**
 * Drag and drop in DIV mode: whole nodes are moved from one zone to another, and
 * where inside the zone they land does not matter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DragDropDivPage extends UrlPage {
	/** The type name of a pet that is still in the shop, and of one that is in the basket. */
	private static final String IN_SHOP = "shop-pet";

	private static final String IN_BASKET = "basket-pet";

	@Override
	public void createContent() throws Exception {
		setPageTitle("Drag and drop: two zones");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Drag and drop between two zones"));

		//-- Both zones are local variables; the handlers below close over them.
		Div basket = new Div("dm-dnd-zone dm-dnd-basket");
		Div shop = new Div("dm-dnd-zone");

		//-- A pet dropped on the basket is added to it, and becomes draggable back to the shop.
		IDragHandler shopDrag = dragHandler(IN_SHOP);
		IDragHandler basketDrag = dragHandler(IN_BASKET);

		basket.setDropHandler(new IDropHandler() {
			@Override
			public String[] getAcceptableTypes() {
				return new String[]{IN_SHOP};
			}

			@Override
			public void onDropped(DropEvent event) throws Exception {
				Div pet = (Div) event.getDraggedNode();
				pet.setDragHandler(basketDrag);
				basket.add(pet);
			}
		});

		shop.setDropHandler(new IDropHandler() {
			@Override
			public String[] getAcceptableTypes() {
				return new String[]{IN_BASKET};
			}

			@Override
			public void onDropped(DropEvent event) throws Exception {
				Div pet = (Div) event.getDraggedNode();
				pet.setDragHandler(shopDrag);
				shop.add(pet);
			}
		});

		cp.add(new Para().add("Drag a pet into the basket, and back out of it again."));
		cp.add(basket);
		cp.add(shop);

		for(int i = 1; i < 16; i++) {
			Div pet = new Div("dm-dnd-item");
			pet.add(new Img("img/dragndrop/drag" + i + ".gif"));
			pet.setDragHandler(shopDrag);
			shop.add(pet);
		}

		cp.add(new Para().add("Any Div can be dragged - setDragHandler() makes it draggable - and "
			+ "any Div can receive droppings - setDropHandler() makes it a drop zone. The two find "
			+ "each other by a type name: the drag handler names the type of the thing being "
			+ "dragged, the drop handler lists the types it accepts, and a zone that does not "
			+ "accept the type does not light up and cannot be dropped on."));

		cp.add(new Para().add("There are two types here, not one: a pet in the shop is a "
			+ "\"shop-pet\" and the basket accepts that; a pet in the basket is a \"basket-pet\" and "
			+ "the shop accepts that. That is why a pet can be dragged in either direction but never "
			+ "dropped back where it already is. Changing what a node is, is a matter of giving it "
			+ "another drag handler when it lands."));

		cp.add(new Para().add("This is a drop zone in DIV mode, the default: the whole zone lights "
			+ "up while an acceptable thing hovers over it, and the drop handler decides where the "
			+ "node ends up - here simply added at the end. Nothing is moved for you: the handler "
			+ "adds the dragged node to the new parent, which takes it out of the old one."));
	}

	/**
	 * A drag handler naming one type. Nothing has to happen when the node is dropped: the
	 * drop handler moves it by adding it somewhere else.
	 */
	private static IDragHandler dragHandler(String type) {
		return new IDragHandler() {
			@Override
			public String getTypeName(NodeBase source) {
				return type;
			}

			@Override
			public IDragArea getDragArea() {
				return null;
			}

			@Override
			public void onDropped(DropEvent event) throws Exception {
			}
		};
	}
}
