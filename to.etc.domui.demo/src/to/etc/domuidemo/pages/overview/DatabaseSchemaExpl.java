package to.etc.domuidemo.pages.overview;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DatabaseSchemaExpl extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new InfoPanel(
"The database used in the examples here is the Chinook database, which can be found at http://chinookdatabase.codeplex.com/. " //
			+ "The database is loaded into a DerbyDB database (an embedded Java SQL database engine) which is created on-the-fly when the demo application is started for the " //
			+ "first time. The demo database represents a 'Music Shop', it contains artists, albums, tracks, and information on their sale." //
			+ "DomUI uses an abstract interface to access the database, and has interfaces for Hibernate and generic SQL implementing that interface." //
			+ "The code here uses Hibernate, and the Chinook database tables are mapped onto Hibernate objects with the same name." //
			+ "For reference, the Chinook database schema is shown below."
		));

		Img img = new Img("img/chinook-schema-1.1.png");
		add(img);
	}

}
