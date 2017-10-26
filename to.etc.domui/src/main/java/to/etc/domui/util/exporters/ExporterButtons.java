package to.etc.domui.util.exporters;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.menu.PopupMenu;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.Msgs;

import java.util.List;

/**
 * Helper class that creates Export buttons, or handles the actions thereof.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class ExporterButtons {
	private ExporterButtons() {
	}

	static public DefaultButton	createExportButton() {
		return new DefaultButton(Msgs.BUNDLE.getString(Msgs.EXPORT_BUTTON), FaIcon.faFileO, a -> exportAction(a));
	}

	public static void exportAction(NodeBase target) {
		List<IExportFormat> exportFormats = ExportFormatRegistry.getExportFormats();
		PopupMenu pm = new PopupMenu();
		for(IExportFormat xf : exportFormats) {
			pm.addItem(xf.extension(), FaIcon.faFile, xf.name(), false, s -> exportSelected(target, xf));
		}
		pm.show(target, null);
	}

	private static void exportSelected(NodeBase node, IExportFormat xf) {
		MsgBox.info(node, "Selected format " + xf.name());
	}


}
