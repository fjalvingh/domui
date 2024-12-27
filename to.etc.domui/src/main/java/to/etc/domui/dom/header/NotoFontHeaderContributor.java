package to.etc.domui.dom.header;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.IBrowserOutput;
import to.etc.domui.dom.IContributorRenderer;
import to.etc.domui.dom.html.OptimalDeltaRenderer;

public class NotoFontHeaderContributor extends HeaderContributor {
	@Override
	public boolean isOfflineCapable() {
		return false;
	}

	@Override
	public void contribute(@NonNull IContributorRenderer r) throws Exception {
		if(r instanceof OptimalDeltaRenderer)
			return;

		IBrowserOutput o = r.o();
		o.tag("link");
		o.attr("rel", "preconnect");
		o.attr("href", "https://fonts.googleapis.com");
		if(r.isXml())
			o.endAndCloseXmltag();
		else
			o.endtag();
		o.dec();

		o.tag("link");
		o.attr("rel", "preconnect");
		o.attr("href", "https://fonts.gstatic.com");
		o.writeRaw(" crossorigin='anonymous'");
		if(r.isXml())
			o.endAndCloseXmltag();
		else
			o.endtag();
		o.dec();

		o.tag("link");
		o.attr("href", "https://fonts.googleapis.com/css2?family=Noto+Sans:ital,wght@0,100..900;1,100..900&display=swap");
		o.attr("rel", "stylesheet");
		if(r.isXml())
			o.endAndCloseXmltag();
		else
			o.endtag();
		o.dec();


	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return false;
	}
}
