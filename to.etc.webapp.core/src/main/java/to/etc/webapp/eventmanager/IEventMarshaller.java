package to.etc.webapp.eventmanager;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Marshal event to string, ready for storing into database as varchar, and
 * recreate event object by unmarshalling string.
 * <br/>
 * <br/>
 * This form is making it easy to update and read from code and from queries.
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 12, 2014
 */
public interface IEventMarshaller {

	/**
	 * Create event object from formated text
	 * @param varchar
	 * @return
	 * @throws Exception
	 */
	@Nullable <T extends AppEventBase> T unmarshalEvent(@NonNull String varchar) throws Exception;

	/**
	 * Convert formated string from provided event
	 * @param event
	 * @return
	 * @throws Exception
	 */
	@NonNull String marshalEvent(@NonNull AppEventBase event) throws Exception;

}
