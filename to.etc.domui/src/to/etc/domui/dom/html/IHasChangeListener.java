package to.etc.domui.dom.html;

/**
 * DomUI nodes that have a change listener.
 * <p>20091120 jal This originally extended INodeErrorDelegate; I removed this because these have nothing to do with eachother.</p>
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 4 Sep 2009
 */
public interface IHasChangeListener {
	IValueChanged< ? , ? > getOnValueChanged();

	void setOnValueChanged(IValueChanged< ? , ? > onValueChanged);
}
