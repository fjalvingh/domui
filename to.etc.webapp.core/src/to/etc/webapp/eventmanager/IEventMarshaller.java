package to.etc.webapp.eventmanager;

import java.util.*;

import javax.annotation.*;

/**
 * Marshal event to string, ready for storing into database as varchar, and
 * recreate event object by unmarshalling string.
 * <br/>
 * The created string is in form<br/>
 * <br/>
 * <b>[event class]</b>TYPE_SEPARATOR<b>([param name]</b>=<b>[param value]</b>PARAM_SEPARATOR<b>...</b>)<br/>
 * e.g class nl.itris.viewpoint.event.EvMessage#messageId=940370383|type=ADDED|
 * <br/>
 * <br/>
 * This form is making it easy to update and read from code and from queries.
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 12, 2014
 */
public interface IEventMarshaller {

	/**
	 * Marker used for separating event type from array of parameters
	 */
	static final String TYPE_SEPARATOR = "#";

	/**
	 * Marker used for separating event parameters between each other
	 */
	static final String PARAM_SEPARATOR = "|";

	/**
	 * List of properties to ignore since they are have dedicated fields in table
	 */
	static final Set<String> SKIP_FIELDS_SET = new HashSet<String>(Arrays.asList("timestamp", "server", "upid"));

	/**
	 * Create event object from formated text
	 * @param varchar
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public <T extends AppEventBase> T unmarshalEvent(@Nonnull String varchar) throws Exception;

	/**
	 * Convert formated string from provided event
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public String marshalEvent(@Nonnull AppEventBase event) throws Exception;

}
